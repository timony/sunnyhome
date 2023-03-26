setInterval(function() {
    refresh();
}, 5500);

$( document ).ready( refresh );

function refresh ( jQuery ) {
    $.ajax({
            type: "GET",
            contentType: "application/json",
            url: "/getinfodata",
            dataType: 'json',
            cache: false,
            timeout: 600000,
            success: function (response) {
                var len = response.length;
                for(var i = 0; i < len; i++){
                    var val = response[i].dataValue;
                    if (response[i].dataValue == "") {
                        val = "-";
                    }
                    var parsedVal = parseFloat(val);
                    $("#"+response[i].dataKey).text(parsedVal.toFixed(2).replace(/[.,]00$/, "") + " " + response[i].units);
                    $("#"+response[i].dataKey).prop('title', "Data Freshness: " + new Date(Date.parse(response[i].dataFreshness)).toLocaleString());

                    if (response[i].dataKey == "solax_exported_power" && parsedVal < 0) {
                        $("#grid_arrow_down").children().hide();
                        $("#grid_arrow_up").children().show();
                    }
                    if (response[i].dataKey == "solax_exported_power" && parsedVal > 0) {
                        $( "#grid_arrow_up" ).children().hide();
                        $( "#grid_arrow_down" ).children().show();
                    }
                    if (response[i].dataKey == "solax_exported_power" && parsedVal == 0) {
                        $("#grid_arrow_down").children().hide();
                        $("#grid_arrow_up").children().hide();
                    }

                    if (response[i].dataKey == "solax_solar_panels_power_total" && parsedVal > 0) {
                        $( "#solar_arrow_down" ).children().show();
                    }
                    if (response[i].dataKey == "solax_solar_panels_power_total" && parsedVal == 0) {
                        $("#solar_arrow_down").children().hide();
                    }

                    if (response[i].dataKey == "solax_battery_power" && parsedVal < 0) {
                        $("#battery_arrow_out").children().hide();
                        $("#battery_arrow_in").children().show();
                    }
                    if (response[i].dataKey == "solax_battery_power" && parsedVal > 0) {
                        $( "#battery_arrow_in" ).children().hide();
                        $( "#battery_arrow_out" ).children().show();
                    }
                    if (response[i].dataKey == "solax_battery_power" && parsedVal == 0) {
                        $("#battery_arrow_in").children().hide();
                        $("#battery_arrow_out").children().hide();
                    }

                    if (response[i].dataKey == "solax_power_consumption_now" && parsedVal > 0) {
                        $( "#house_arrow_out" ).children().show();
                    }
                    if (response[i].dataKey == "solax_power_consumption_now" && parsedVal == 0) {
                        $("#house_arrow_out").children().hide();
                    }

                }
            }
        });
}