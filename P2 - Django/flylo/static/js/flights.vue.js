Vue.component('flight', {
    props: ['flight'],

    data: function () {
        return {
            added: false,
            seats: 1,
            type: 'e',
            airline: this.flight.airlines[0].code,
            url: context.departure ? '../'+this.flight.pk : './'+this.flight.pk,
            classes: [
                {name: 'Economy', code: 'e'},
                {name: 'Business', code: 'b'},
                {name: 'First Class', code: 'f'}
            ]
        }
    },

    computed: {
        price: function () {
            type = this.type === 'e' ? 1 : this.type ===  'b' ? 1.5 : 2.5;
            return (this.flight.price * this.seats * type).toFixed(2);
        }
    },


    template: '<tr :class="{added : added}">' +
        '<td class="h4"><input type="hidden" :name="\'selected_going\'+flight.pk" :value="flight.pk" v-if="added">' +
        '<a class="label label-success" :href="url">{{ flight.flight_number }}</a></td>' +
        '<td style="vertical-align: middle"><input type="checkbox" :name="\'return_flight\'+flight.pk" :value="flight.pk"></td>' +
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
            vue.$emit('add-cart', pk)
            this.added = true;
        },

        removeCart: function (pk) {
            vue.$emit('remove-cart', pk)
            this.added = false;
        }
    }
});

vue = new Vue({
    delimiters: ['${', '}'],
    el: '#app',
    data: {
        added: [],
        departure: context.departure,
        flights: []
    },
    methods: {
        updateFlights: function () {
            url = 'http://localhost:8080/api/flights';
            if (this.departure)
                url += '?departure=' + this.departure;

            $.get(url, function (data) {
                vue.flights = data
            });
        },

    },
    created: function () {
        this.updateFlights()

        this.$on('add-cart', function (pk) {
            this.added.push(pk);
        });

        this.$on('remove-cart', function (pk) {
            var index = this.added.indexOf(pk);
            this.added.splice(index, 1);
        })

    }

});