function loadUser() {
    let r = jsRoutes.controllers.UserController.get();
    $.ajax({
        url: r.url,
        type: r.type,
        contentType: "application/json",
        success: (data) => {
            console.log(data);
            $('#name').val(data.name);
            $('#password').val("************");
        },
        error: (data) => console.error(data),
        dataType: "json"
    });
}

$(document).ready(() => {
        loadUser();
    }
)