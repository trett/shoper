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
    let inp = $('<textarea>').attr({
        // type: 'text',
        id: status + "-" + id,
        rows: 2,
        class: 'form-control',
        placeholder: 'Product'
    }).val(name).prop('disabled', saved)
    let ig = $('<div>').attr({class: 'input-group mb-2'});
    $('#purchaseList').append(ig);
    ig.append(inp);
    if (saved) {
        ig.attr({id: 'ig-' + id});
        ig.prepend(`<div class='input-group-prepend' id='ipg-${id}'><div class='input-group-text'>` +
            `<button class="btn btn-outline-secondary" type="button" id='cb-${id}'>Done</button></div></div>`);
        // `<input type='checkbox' id='cb-${id}' aria-label='Mark as done'></div></div>`);
        ig.append("<div class='input-group-append'><button class='btn btn-outline-secondary' " +
            `type='button' id='db-${id}'><i class='fas fa-trash'></i></button></div>`);
        $('#db-' + id).click(() => deleteItem(id));
        $('#cb-' + id).click(() => updateStatus(status, id))
            .text(done ? "Undo" : "Done")
            .addClass(done ? 'btn-outline-secondary' : "btn-outline-success");
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
    let items = $('#purchaseList textarea').toArray()
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
            loadData();
        },
        error: (data) => console.log(data)
    });
}

function updateStatus(status, id) {
    let r = jsRoutes.controllers.PurchaseController.update();
    let source = $(`#cb-${id}`);
    let done = source.text() === "Done";
    $.ajax({
        url: r.url,
        type: r.type,
        data: `{"id": ${id}, "status": "${done ? 'DONE' : 'TODO'}"}`,
        contentType: "application/json",
        success: (data) => {
            if (done) {
                $(`#${status}-${id}`).css('text-decoration', 'line-through');
            } else {
                $(`#${status}-${id}`).css('text-decoration', '');
            }
            source.text(done ? "Undo" : "Done");
            source.removeClass("btn-outline-success btn-outline-secondary");
            source.addClass(done ? 'btn-outline-secondary' : "btn-outline-success");
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