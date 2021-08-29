let requestData = {newItems: [], idsForDelete: []};

function loadData() {
    let r = jsRoutes.controllers.PurchaseController.load();
    $.ajax({
        url: r.url,
        type: r.type,
        contentType: "application/json",
        success: (data) => addRows(data),
        error: (data) => console.error(data),
        dataType: "json"
    });
    disableSaveButton(true);
}

function addRows(rows) {
    $('#purchaseList').empty();
    rows.forEach(row => addRow(row.id, row.name, row.status));
}

function addRow(id, name, status) {
    let todo = status === "TODO";
    let done = status === "DONE";
    let saved = todo || done;
    let inp = $('<input>').attr({
        type: 'text',
        id: status + "-" + id,
        value: name,
        class: 'form-control',
        placeholder: 'Product'
    }).prop('disabled', saved);
    let ig = $('<div>').attr({class: 'input-group'});
    $('#purchaseList').append(ig);
    ig.append(inp);
    if (saved) {
        ig.attr({id: 'ig-' + id});
        ig.prepend("<div class='input-group-prepend'><div class='input-group-text'>" +
            `<input type='checkbox' id='cb-${id}' aria-label='Mark as done'></div></div>`);
        ig.append("<div class='input-group-append'><button class='btn btn-outline-danger' " +
            `type='button' id='db-${id}'><i class='fas fa-trash'></i></button></div>`);
        $('#db-' + id).click(() => deleteItem(id));
        $('#cb-' + id).click(() => updateStatus(status, id)).prop('checked', done);
        if (done) {
            inp.css('text-decoration', 'line-through');
        }
    }
    disableSaveButton(saved);
}

function deleteItem(id) {
    requestData.idsForDelete.push(id);
    $('#ig-' + id).remove();
    disableSaveButton(false);
}

function saveData() {
    let items = $('#purchaseList :input[type=text]').toArray()
        .filter(item => item.id.split('-')[0] === "NEW")
        .filter(item => !!item.value)
    items.forEach(el => requestData.newItems.push({name: el.value, status: "TODO"}));
    if (isRequestDataIsEmpty()) return;
    let r = jsRoutes.controllers.PurchaseController.save(requestData);
    $.ajax({
        url: r.url,
        type: r.type,
        data: JSON.stringify(requestData),
        contentType: "application/json",
        success: (data) => {
            requestData.newItems.length = 0;
            requestData.idsForDelete.length = 0;
            // addRows(data);
            loadData();
        },
        error: (data) => console.log(data)
    });
}

function updateStatus(status, id) {
    let r = jsRoutes.controllers.PurchaseController.update();
    let done = $(`#cb-${id}`).is(':checked');
    $.ajax({
        url: r.url,
        type: r.type,
        data: `{"id": ${id}, "status": "${done ? 'DONE' : 'TODO'}"}`,
        contentType: "application/json",
        success: (data) => {
            if (done) {
                $(`#${status}-${id}`).css('text-decoration', 'line-through')
            } else {
                $(`#${status}-${id}`).css('text-decoration', '')
            }
        }
    })
}

function disableSaveButton(disabled) {
    $('#save-button').prop('disabled', disabled);
}

function isRequestDataIsEmpty() {
    return requestData.newItems.length < 1 && requestData.idsForDelete.length < 1;
}

export {loadData, addRow, saveData};