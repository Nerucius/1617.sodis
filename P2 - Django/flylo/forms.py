from django import forms
from models import *


class SelectDepartureForm(forms.Form):
	departures = Flight.objects.order_by('location_departure').values_list('location_departure').distinct()
	departures = [departures[i][0] for i in range(len(departures))]

	DEPARTURE_CHOICES = [(i+1, departures[i]) for i in range(len(departures))]
	departure = forms.ChoiceField(DEPARTURE_CHOICES)

	def get_departure(self, id):
		return self.departures[int(id)-1]
