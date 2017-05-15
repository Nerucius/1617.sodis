from django import forms
from models import *


class SelectDepartureForm(forms.Form):
	attrs = {
		'class': 'form-control'
	}

	departures = Flight.objects.order_by('location_departure').values_list('location_departure').distinct()
	departures = [departures[i][0] for i in range(len(departures))]
	DEPARTURE_CHOICES = [(i + 1, departures[i]) for i in range(len(departures))]

	# Fields
	departure = forms.ChoiceField(DEPARTURE_CHOICES, widget=forms.Select(attrs=attrs))

	def get_departure(self, id):
		return self.departures[int(id) - 1]
