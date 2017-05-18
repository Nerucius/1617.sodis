Vue.component('flight', {
    props: {
        flight: {default: {}},
        show_return: {default: 'true'}
    },

    data: function () {
        return {
            added: false,
            seats: 1,
            type: 'e',
            airline: this.flight.airlines[0].code,
            url: this.departure ? '../' + this.flight.pk : './' + this.flight.pk,
            classes: [
                {name: 'Economy', code: 'e'},
                {name: 'Business', code: 'b'},
                {name: 'First Class', code: 'f'}
            ]
        }
    },

    computed: {
        price: function () {
            console.log("compute price for " + this.airline);

            var acode = this.airline;
            var type = this.type === 'e' ? 1 : this.type === 'b' ? 1.5 : 2.5;
            var aprice = 0;
            var nseats = this.seats;

            this.flight.airlines.forEach(function(a){

                if (a.code == acode) {
                    console.log("found airline: "+a.code);
                    aprice = parseFloat(a.price) * nseats;
                }

            });

            return ( this.flight.price * this.seats * type + aprice).toFixed(2);
        }
    },


    template: '<tr :class="{added : added}">' +
            '<td class="h4"><input type="hidden" :name="\'selected_going\'+flight.pk" :value="flight.pk" v-if="added">' +
            '<a class="label label-success" :href="url">{{ flight.flight_number }}</a></td>' +
            '<td v-if="show_return == \'true\'" style="vertical-align: middle"><input type="checkbox" :name="\'return_flight\'+flight.pk" :value="flight.pk"></td>' +
            '<td class="h4"><b class="label label-primary">{{ flight.location_departure }}</b></td>' +
            '<td class="h4"><b class="label label-primary">{{ flight.location_arrival }}</b></td>' +
            '<td><select :name="\'airline\'+flight.pk" class="form-control input-sm" v-model="airline" :readonly="added">' +
            '       <option v-for="a in flight.airlines" :value="a.code" >{{a.code}}</option>' +
            '    </select>' +
            '</td>' +
            '<td><select :name="\'seats\'+flight.pk" class="form-control input-sm" v-model="seats" :readonly="added">' +
            '       <option v-for="n in 5" :value="n" >{{n}} Seats</option>' +
            '    </select>' +
            '</td>' +
            '<td><select :name="\'type\'+flight.pk" class="form-control input-sm" v-model="type" :readonly="added">' +
            '       <option v-for="c in classes" :value="c.code" >{{c.name}}</option>' +
            '    </select>' +
            '</td>' +
            '<td style="padding-top: 3px" class="h3"><span class="label label-success">{{ price }}&euro;</span></td>' +
            '<td><span @click="addCart(flight.pk)" class="btn btn-sm btn-warning" v-if="!added">Add to Cart</span>' +
            '    <span @click="removeCart(flight.pk)" class="btn btn-sm btn-danger" v-if="added">Remove</span></td>' +
            '</tr>',

    methods: {
        addCart: function (pk) {
            this.added = true;
        },

        removeCart: function (pk) {
            this.added = false;
        }
    }
});


Vue.component('flight-table', {
    props: {
        show_return: {default: 'true'},
        departure: {default: null},
        arrival: {default: null},
    },

    data: function () {
        return {
            flights: []
        }
    },

    template: "<table class='table table-striped table-hover table-responsive'>" +
              "<thead>" +
              "<tr>" +
                "<th>FN</th>" +
                "<th v-if='show_return == \"true\"'>Return?</th>" +
                "<th>Departure</th>" +
                "<th>Arrival</th>" +
                "<th>Airline</th>" +
                "<th>No. Seats</th>" +
                "<th>Class</th>" +
                "<th>Price</th>" +
                "<th></th>" +
              "</tr>" +
              "</thead>" +
              "<tbody is='transition-group' name='fade'>" +
                "<tr is='flight' v-for='f in flights' :key='f.pk' :flight='f' :show_return='show_return'></tr>" +
              "</tbody>" +
              "</table>",

    methods: {
        updateFligths: function () {
            console.log(this)
            var url = 'http://localhost:8080/api/flights';
            // Dep and Arr filters
            if (this.departure)
                url += '?departure=' + this.departure;
            if (this.arrival)
                url += '&arrival=' + this.arrival;

            $.get(url, function (data) {
                this.flights = data;
            }.bind(this));
        }
    }

});