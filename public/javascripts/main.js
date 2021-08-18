import {loadData, addRow, saveData} from './purchases.js'

$(document).ready(() => {
        loadData();
        $('#add-row-button').click(() => addRow('0', '', 'NEW'));
        $('#save-button').click(() => saveData());
    }
)
