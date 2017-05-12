from rest_framework import routers, serializers, viewsets
from flylo.models import *

class AirlineSerializer(serializers.HyperlinkedModelSerializer):
	class Meta:
		model = Airline
		fields = ('code',)


class AirplaneSerializer(serializers.HyperlinkedModelSerializer):
	class Meta:
		model = Airplane
		fields = ('aircraft', 'seats_economy', 'seats_business', 'seats_first_class')


# Serializers define the API representation.
class FlightSerializer(serializers.HyperlinkedModelSerializer):
	airplane = AirplaneSerializer(read_only=True)
	airlines = AirlineSerializer(many=True, read_only=True)
	price = serializers.DecimalField(12, 2, read_only=True)

	class Meta:
		model = Flight
		fields = ('flight_number', 'estimated_time_departure',
				  'estimated_time_arrival', 'location_departure',
				  'location_arrival', 'status', 'price',
				  'airplane', 'airlines')
