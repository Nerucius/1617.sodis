from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.views.generic import ListView

from .models import *
from .forms import *

# Index view with a request, and a render response to index.html


def index(request):
	context = {}
	# In case the form has been completed, redirect to the filtered by departure flights page
	if request.method == 'POST':
		form = SelectDepartureForm(request.POST)
		if form.is_valid():
			id = form.cleaned_data.get('departure')
			departure = form.get_departure(id)
			return HttpResponseRedirect('flights/' + departure)

	# If not, send the empty form
	else:
		context['form'] = SelectDepartureForm()
	return render(request, 'index.html', context)


def flights(request, departure=None):
	context = {}

	if departure:
		context['departure'] = departure
		context['flights'] = Flight.objects.filter(location_departure=departure)

	else:
		context['flights'] = Flight.objects.all()

	return render(request, 'flights.html', context)


def simple_flight(request, pk=None):
	context = {}
	context['simple_flight'] = Flight.objects.values('flight_number', 'location_departure', 'location_arrival', 'status')

	if pk:
		context['simple_flight'] = context['simple_flights'].get(pk=pk)

	return render(request, 'html', context)


# TODO use
# TODO add airplane info
# TODO add airline info
# This view returns a flight with all the available information, if pk is given,
# or the list of all existing flights with all the available information, if not.
'''def detailed_flights(request, pk=None):
	context = {}
	context['detailed_flights'] = Flight.objects.all()

	if pk:
		context['detailed_flights'] = context['detailed_flights'].get(pk=pk)

	return render(request, 'html', context)'''

'''
def flights(request, departure=None):
	context = {}

	if departure:
		context['departure'] = departure
		context['flights'] = Flight.objects.filter(location_departure=departure)
	else:
		context['flights'] = Flight.objects.all()

	return render(request, 'flights.html', context'''
