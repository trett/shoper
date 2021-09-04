import {loadData, addRow, saveData} from './purchases.js'

$(document).ready(() => {
        loadData();
        $('#add-row-button').click(() => addRow());
        $('#save-button').click(() => saveData());
    }
)
