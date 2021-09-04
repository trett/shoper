let requestData = {newItems: [], idsForDelete: []};

function loadData() {
    let r = jsRoutes.controllers.PurchaseController.load();
    $.ajax({
        url: r.url,
        type: r.type,
        contentType: "application/json",
        success: (data) => renderRows(data),
        error: (data) => console.error(data),
        dataType: "json"
    });
    disableSaveButton(true);
}

function renderRows(data) {
    fetch('/assets/templates/purchase.mustache')
        .then((response) => response.text())
        .then((template) => {
            const rendered = Mustache.render(template, {
                'items': data,
                'buttonText': function () {
                    return getButtonText(this.status);
                },
                'buttonColor': function () {
                    return getButtonColor(this.status);
                }
            });
            document.getElementById('purchaseList').innerHTML = rendered;
        }).then(() => {
        $('#purchaseList .input-group-prepend').toArray().forEach(item => {
            const id = item.id.split("-")[1];
            if (!id) {
                return;
            }
            // update decoration
            strike(id, $('#ta-' + id).data('status'));
            // add event handlers
            $('#cb-' + id).click(() => updateStatus(item.innerText, id));
            $('#db-' + id).click(() => deleteItem(Number(id)));
        })
    })
}

function addRow() {
    const row = $('<textarea>').attr({
        'data-status': "NEW",
        rows: 2,
        class: 'form-control',
        placeholder: 'Product'
    })
    $('#purchaseList').append(row);
    disableSaveButton(false);
}

function deleteItem(id) {
    requestData.idsForDelete.push(id);
    $('#ig-' + id).remove();
    disableSaveButton(false);
}

function saveData() {
    let items = $('#purchaseList textarea').toArray()
        .filter(item => item.dataset.status === "NEW")
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
    $.ajax({
        url: r.url,
        type: r.type,
        data: `{"id": ${id}, "status": "${status}"}`,
        contentType: "application/json",
        success: (data) => {
            strike(id, status);
            source.text(getButtonText(status));
            source.removeClass("btn-outline-success btn-outline-secondary");
            source.addClass(getButtonColor(status));
        }
    })
}

function disableSaveButton(disabled) {
    $('#save-button').prop('disabled', disabled);
}

function isRequestDataIsEmpty() {
    return requestData.newItems.length < 1 && requestData.idsForDelete.length < 1;
}

function strike(id, status) {
    $(`#ta-${id}`).css('text-decoration', status === "DONE" ? 'line-through' : 'none');
}

function getButtonText(status) {
    return status === "DONE" ? "TODO" : "DONE";
}

function getButtonColor(status) {
    return status === "DONE" ? "btn-outline-secondary" : "btn-outline-success";
}

export {loadData, addRow, saveData};