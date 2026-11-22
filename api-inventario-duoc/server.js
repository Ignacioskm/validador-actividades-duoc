const express = require('express');
const cors = require('cors');
const oracledb = require('oracledb');
const { initDB, getConnection } = require('./db');

const app = express();
app.use(cors());
app.use(express.json());

// 1. Listar recursos activos (opcionalmente filtrados por departamento)
app.get('/api/recursos', async (req, res) => {
    const { departamento } = req.query;
    let conn;
    try {
        conn = await getConnection();
        const sql = departamento
            ? `SELECT id AS "id", departamento AS "departamento", nombre_item AS "nombreItem",
                stock_total AS "stockTotal", activo AS "activo"
         FROM recursos_inventario
         WHERE activo = 1 AND departamento = :departamento
         ORDER BY nombre_item`
            : `SELECT id AS "id", departamento AS "departamento", nombre_item AS "nombreItem",
                stock_total AS "stockTotal", activo AS "activo"
         FROM recursos_inventario
         WHERE activo = 1
         ORDER BY departamento, nombre_item`;

        const binds = departamento ? { departamento } : {};
        const result = await conn.execute(sql, binds);
        const recursos = result.rows.map((r) => ({ ...r, activo: r.activo === 1 }));
        res.json(recursos);
    } catch (err) {
        res.status(500).json({ error: err.message });
    } finally {
        if (conn) await conn.close();
    }
});

// 2. Consultar disponibilidad de TODOS los recursos para una fecha y rango de bloques
// (Importante: debe ir antes de cualquier ruta con :id)
app.get('/api/recursos/disponibilidad', async (req, res) => {
    const { fecha, bloqueInicio, bloqueFin } = req.query;
    const bInicio = Number(bloqueInicio);
    const bFin = Number(bloqueFin);

    if (!fecha || !bInicio || !bFin || bFin < bInicio) {
        return res.status(400).json({ error: 'Parámetros inválidos: fecha, bloqueInicio y bloqueFin' });
    }

    let conn;
    try {
        conn = await getConnection();
        const result = await conn.execute(
            `WITH bloques AS (
                SELECT :bInicio + LEVEL - 1 AS bloque
                FROM dual
             CONNECT BY LEVEL <= (:bFin - :bInicio + 1)
                 ),
                 ocupacion_por_bloque AS (
             SELECT c.recurso_id, b.bloque, SUM(c.cantidad) AS ocupado_bloque
             FROM bloques b
                 JOIN compromisos_stock c
             ON c.fecha = TO_DATE(:fecha, 'YYYY-MM-DD')
                 AND c.estado = 'ACTIVO'
                 AND b.bloque BETWEEN c.bloque_inicio AND c.bloque_fin
             GROUP BY c.recurso_id, b.bloque
                 ),
                 max_ocupacion AS (
             SELECT recurso_id, MAX(ocupado_bloque) AS max_ocupado
             FROM ocupacion_por_bloque
             GROUP BY recurso_id
                 )
            SELECT r.id AS "id",
                   r.departamento AS "departamento",
                   r.nombre_item AS "nombreItem",
                   r.stock_total AS "stockTotal",
                   r.activo AS "activo",
                   GREATEST(0, r.stock_total - NVL(m.max_ocupado, 0)) AS "stockDisponible"
            FROM recursos_inventario r
                     LEFT JOIN max_ocupacion m ON m.recurso_id = r.id
            WHERE r.activo = 1
            ORDER BY r.departamento, r.nombre_item`,
            { fecha, bInicio, bFin }
        );

        const recursos = result.rows.map((r) => ({ ...r, activo: r.activo === 1 }));
        res.json(recursos);
    } catch (err) {
        res.status(500).json({ error: err.message });
    } finally {
        if (conn) await conn.close();
    }
});

// 3. Crear un nuevo recurso
app.post('/api/recursos', async (req, res) => {
    const { departamento, nombreItem, stockTotal } = req.body;
    if (!departamento || !nombreItem || stockTotal == null || stockTotal < 0) {
        return res.status(400).json({ error: 'Datos de recurso inválidos' });
    }

    let conn;
    try {
        conn = await getConnection();
        const result = await conn.execute(
            `INSERT INTO recursos_inventario (departamento, nombre_item, stock_total, activo)
       VALUES (:departamento, :nombreItem, :stockTotal, 1)
       RETURNING id INTO :id`,
            {
                departamento,
                nombreItem: nombreItem.trim(),
                stockTotal: Number(stockTotal),
                id: { dir: oracledb.BIND_OUT, type: oracledb.NUMBER },
            },
            { autoCommit: true }
        );

        res.status(201).json({
            id: result.outBinds.id[0],
            departamento,
            nombreItem: nombreItem.trim(),
            stockTotal: Number(stockTotal),
            activo: true,
        });
    } catch (err) {
        res.status(400).json({ error: err.message });
    } finally {
        if (conn) await conn.close();
    }
});

// 4. Actualizar stock total de un recurso
app.patch('/api/recursos/:id/stock', async (req, res) => {
    const id = Number(req.params.id);
    const { stockTotal } = req.body;
    if (stockTotal == null || stockTotal < 0) {
        return res.status(400).json({ error: 'stockTotal debe ser mayor o igual a 0' });
    }

    let conn;
    try {
        conn = await getConnection();
        const updateRes = await conn.execute(
            `UPDATE recursos_inventario SET stock_total = :stockTotal WHERE id = :id`,
            { stockTotal: Number(stockTotal), id },
            { autoCommit: true }
        );

        if (updateRes.rowsAffected === 0) {
            return res.status(404).json({ error: 'Recurso no encontrado' });
        }

        const selectRes = await conn.execute(
            `SELECT id AS "id", departamento AS "departamento", nombre_item AS "nombreItem",
              stock_total AS "stockTotal", activo AS "activo"
       FROM recursos_inventario WHERE id = :id`,
            { id }
        );

        const row = selectRes.rows[0];
        res.json({ ...row, activo: row.activo === 1 });
    } catch (err) {
        res.status(400).json({ error: err.message });
    } finally {
        if (conn) await conn.close();
    }
});

// 5. Desactivar recurso (borrado lógico)
app.delete('/api/recursos/:id', async (req, res) => {
    const id = Number(req.params.id);
    let conn;
    try {
        conn = await getConnection();
        const result = await conn.execute(
            `UPDATE recursos_inventario SET activo = 0 WHERE id = :id`,
            { id },
            { autoCommit: true }
        );

        if (result.rowsAffected === 0) {
            return res.status(404).json({ error: 'Recurso no encontrado' });
        }
        res.status(204).send();
    } catch (err) {
        res.status(500).json({ error: err.message });
    } finally {
        if (conn) await conn.close();
    }
});

// 6. Comprometer stock (transacción atómica anti sobre-stock)
app.post('/api/compromisos', async (req, res) => {
    const { reservaRef, fecha, bloqueInicio, bloqueFin, items } = req.body;
    if (!reservaRef || !fecha || !bloqueInicio || !bloqueFin || !Array.isArray(items) || items.length === 0) {
        return res.status(400).json({ error: 'Faltan datos obligatorios para comprometer stock' });
    }

    let conn;
    try {
        conn = await getConnection();

        for (const item of items) {
            const recursoId = Number(item.recursoId);
            const cantidad = Number(item.cantidad);

            // Bloquea la fila del recurso hasta el commit/rollback para evitar concurrencia
            const recRes = await conn.execute(
                `SELECT id, nombre_item, stock_total
         FROM recursos_inventario
         WHERE id = :recursoId AND activo = 1
         FOR UPDATE`,
                { recursoId }
            );

            if (recRes.rows.length === 0) {
                throw new Error(`El recurso ${recursoId} no existe o está inactivo`);
            }

            const recurso = recRes.rows[0];
            let maxOcupado = 0;

            for (let b = Number(bloqueInicio); b <= Number(bloqueFin); b++) {
                const occRes = await conn.execute(
                    `SELECT NVL(SUM(cantidad), 0) AS ocupado
           FROM compromisos_stock
           WHERE recurso_id = :recursoId
             AND fecha = TO_DATE(:fecha, 'YYYY-MM-DD')
             AND estado = 'ACTIVO'
             AND :bloque BETWEEN bloque_inicio AND bloque_fin`,
                    { recursoId, fecha, bloque: b }
                );
                maxOcupado = Math.max(maxOcupado, occRes.rows[0].OCUPADO);
            }

            const disponible = recurso.STOCK_TOTAL - maxOcupado;
            if (cantidad > disponible) {
                throw new Error(`Stock insuficiente de '${recurso.NOMBRE_ITEM}' (disponible: ${disponible})`);
            }

            await conn.execute(
                `INSERT INTO compromisos_stock
           (reserva_ref, recurso_id, fecha, bloque_inicio, bloque_fin, cantidad, estado)
         VALUES
           (:reservaRef, :recursoId, TO_DATE(:fecha, 'YYYY-MM-DD'), :bloqueInicio, :bloqueFin, :cantidad, 'ACTIVO')`,
                {
                    reservaRef,
                    recursoId,
                    fecha,
                    bloqueInicio: Number(bloqueInicio),
                    bloqueFin: Number(bloqueFin),
                    cantidad,
                }
            );
        }

        await conn.commit();
        res.status(201).json({ ok: true, reservaRef });
    } catch (err) {
        if (conn) await conn.rollback();
        res.status(409).json({ error: err.message });
    } finally {
        if (conn) await conn.close();
    }
});

// 7. Liberar stock de una reserva rechazada o cancelada
app.patch('/api/compromisos/:reservaRef/liberar', async (req, res) => {
    const { reservaRef } = req.params;
    let conn;
    try {
        conn = await getConnection();
        await conn.execute(
            `UPDATE compromisos_stock
       SET estado = 'LIBERADO'
       WHERE reserva_ref = :reservaRef AND estado = 'ACTIVO'`,
            { reservaRef },
            { autoCommit: true }
        );
        res.status(204).send();
    } catch (err) {
        res.status(500).json({ error: err.message });
    } finally {
        if (conn) await conn.close();
    }
});

const PORT = process.env.PORT || 3000;

initDB()
    .then(() => {
        app.listen(3000, '0.0.0.0', () =>
            console.log(`API corriendo en http://0.0.0.0:${PORT}`));
    })
    .catch((err) => {
        console.error('Error conectando a Oracle:', err);
        process.exit(1);
    });