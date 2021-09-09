const requestData = { newItems: [], idsForDelete: [] };

function loadData() {
    const r = jsRoutes.controllers.PurchaseController.load();
    fetch(r.url)
        .then(response => response.json())
        .then(data => renderRows(data))
        .catch(err => console.log(err));
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
    const el = $('#purchase-' + id);
    el.hide('slow', () => el.remove());
    disableSaveButton(false);
}

function saveData() {
    const items = $('#purchaseList textarea').toArray()
        .filter(item => item.dataset.status === "NEW")
        .filter(item => !!item.value)
    items.forEach(el => requestData.newItems.push({ name: el.value, status: "TODO" }));
    if (isRequestDataIsEmpty()) return;
    const r = jsRoutes.controllers.PurchaseController.save(requestData);
    fetch(r.url, {
        method: r.type,
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify(requestData)
    }).then(response => {
        requestData.newItems.length = 0;
        requestData.idsForDelete.length = 0;
        loadData();
    }).catch(err => console.log(err));
}

function updateStatus(status, id) {
    const r = jsRoutes.controllers.PurchaseController.update();
    const source = $(`#cb-${id}`);
    fetch(r.url, {
        method: r.type,
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: `{"id": ${id}, "status": "${status}"}`
    }).then(response => {
        strike(id, status);
        source.text(getButtonText(status));
        source.removeClass("btn-outline-success btn-outline-secondary");
        source.addClass(getButtonColor(status));
    }).catch(err => console.log(err));
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

export { loadData, addRow, saveData };