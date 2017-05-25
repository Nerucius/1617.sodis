from rest_framework import routers, serializers, viewsets
from flylo.models import *


class AirlineSerializer(serializers.ModelSerializer):
	class Meta:
		model = Airline
		# fields = serializers.ALL_FIELDS
		fields = ('code', 'price')


class AirplaneSerializer(serializers.ModelSerializer):
	class Meta:
		model = Airplane
		fields = ('aircraft', 'seats_economy', 'seats_business', 'seats_first_class')


class FlightSerializer(serializers.ModelSerializer):
	# NOTE: removed read_only=True from airplane, airlines
	airplane = AirplaneSerializer()
	airlines = AirlineSerializer(many=True, )
	# price = serializers.DecimalField(12, 2, read_only=True)
	price = serializers.FloatField(read_only=True)
	computed_price = serializers.DecimalField(12, 2, read_only=True)

	class Meta:
		model = Flight
		fields = ('pk', 'flight_number', 'estimated_time_departure',
				  'estimated_time_arrival', 'location_departure',
				  'location_arrival', 'status', 'price', 'computed_price',
				  'airplane', 'airlines')
