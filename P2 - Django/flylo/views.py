from django.shortcuts import render
from django.http import HttpResponse
from .models import *


# Index view with a request, and a render response to index.html


def index(request):
	context = {}
	context['departures'] = Flight.objects.order_by().values_list('location_departure', flat=True).distinct()
	return render(request, 'index.html', context)


# TODO use
# This view returns a flight with the basic information, if pk is given,
# or the list of all existing flights with the basic information, if not.
def simple_flights(request, pk=None):
	context = {}
	context['simple_flights'] = Flight.objects.values('flight_number', 'location_departure', 'location_arrival', 'status')

	if pk:
		context['simple_flights'] = context['simple_flights'].get(pk=pk)

	return render(request, 'html', context)


# TODO use
# TODO add airplane info
# TODO add airline info
# This view returns a flight with all the available information, if pk is given,
# or the list of all existing flights with all the available information, if not.
def detailed_flights(request, pk=None):
	context = {}
	context['detailed_flights'] = Flight.objects.all()

	if pk:
		context['detailed_flights'] = context['detailed_flights'].get(pk=pk)

	return render(request, 'html', context)


def flights(request, departure=None):
	context = {}

	if departure:
		context['departure'] = departure
		context['flights'] = Flight.objects.filter(location_departure=departure)
	else:
		context['flights'] = Flight.objects.all()

	return render(request, 'flights.html', context)
