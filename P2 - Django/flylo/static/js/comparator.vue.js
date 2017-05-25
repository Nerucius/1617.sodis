Vue.component('flight-panel', {
    props: ['flight', 'compare_price', 'is_cheapest'],

    template: `
        <div :class="panel_class">
            <div class="panel-heading" style="font-size: 1.2em"><span class="glyphicon glyphicon-plane"></span>
                {{ flight.flight_number }} <small class="pull-right">site: <a :href="object_url">{{ domain_url }}</a></small>
            </div>
            
            <table class="table text-center">
                <thead>
                <tr>
                    <th>Departure</th>
                    <th>Arrival</th>
                </tr>
                </thead>
                <tr class="h3">
                    <td>{{ flight.location_departure }}</td>
                    <td>{{ flight.location_arrival }}</td>
                </tr>
                <tr class="h4">
                    <td>{{ time_departure }}</td>
                    <td>{{ time_arrival }}</td>
                </tr>
                <tr>
                    <td colspan="2" class="h3" style="padding: 20px"><span
                            :class="compare_class">{{ flight.price }}&euro;</span></td>
                </tr>
            </table>
            
        </div>
    `,

    computed: {
        time_departure: function () {
            return moment(this.flight.estimated_time_departure).format("Y/M/D hh:mm")
        },

        time_arrival: function () {
            return moment(this.flight.estimated_time_arrival).format("Y/M/D hh:mm")

        },

        panel_class: function () {
            if (this.flight.cheapest === true || this.is_cheapest === true)
                return "panel panel-success";
            else
                return "panel panel-default";
        },

        compare_class: function () {
            if (this.flight === undefined)
                return "";

            if (this.compare_price === undefined)
                return "label label-warning"
            else {
                if (this.flight.price >= this.compare_price)
                    return "label label-danger"
                else
                    return "label label-success"
            }
        },

        object_url: function(){
            return this.flight.url+this.flight.pk;
        },

        domain_url: function () {
            let pat = /^(?:https?:\/\/)?(?:[^@\/\n]+@)?(?:www\.)?([^:\/\n]+)/i;
            let arr = pat.exec(this.flight.url);
            console.log(arr);
            if (arr[1] !== undefined)
                return arr[1]
            return "";
        }




    }
})
;