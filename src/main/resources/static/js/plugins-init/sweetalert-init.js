function confirmDelete() {
        const queryString = window.location.search;
        const urlParams = new URLSearchParams(queryString);

        Swal.fire({
            title: "Are you sure to delete?",
            text: "You will not be able to recover this record !!",
            type: "warning",
            showCancelButton: !0,
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "Yes, delete it !!"
        }).then((result) => {
            if (result.isConfirmed) {
              window.location.href = '//' + location.host + location.pathname + '/edit?id=' + urlParams.get('id') + '&delete=true';
            }
          })
};

function confirmManualOverride(id, cancel) {
        const queryString = window.location.search;
        const urlParams = new URLSearchParams(queryString);

        if (cancel == false) {
            Swal.fire({
                title: "Are you sure to manually override?",
                text: "This will toggle the device switch and override the automation for next hour or until you decide to cancel the override.",
                type: "warning",
                showCancelButton: !0,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: "Yes, proceed."
            }).then((result) => {
                if (result.isConfirmed) {
                  $.get('/toggle?id=' + id);
                  setTimeout(function(){
                     window.location.reload();
                  }, 1000);
                }
              })
        }
        if (cancel == true) {
            Swal.fire({
                title: "Cancel manual override?",
                text: "This action will cancel manual override and return to automatic device control.",
                type: "warning",
                showCancelButton: !0,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: "Yes, proceed."
            }).then((result) => {
                if (result.isConfirmed) {
                  $.get('/toggle?id=' + id);
                  setTimeout(function(){
                     window.location.reload();
                  }, 1000);
                }
              })
        }
};