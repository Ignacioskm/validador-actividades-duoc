const oracledb = require('oracledb');
require('dotenv').config();

// Devuelve las filas como objetos JSON { ID: 1, NOMBRE_ITEM: 'Sillas', ... }
oracledb.outFormat = oracledb.OUT_FORMAT_OBJECT;

async function initDB() {
    await oracledb.createPool({
        user: process.env.DB_USER,
        password: process.env.DB_PASSWORD,
        connectString: process.env.DB_CONNECT_STRING,
        configDir: process.env.WALLET_DIR,
        walletLocation: process.env.WALLET_DIR,
        walletPassword: process.env.WALLET_PASSWORD,
    });
    console.log('Conectado a Oracle Cloud Autonomous Database');
}

async function getConnection() {
    return await oracledb.getConnection();
}

module.exports = { initDB, getConnection };