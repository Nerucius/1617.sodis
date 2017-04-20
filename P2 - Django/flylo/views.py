from django.shortcuts import render
from django.http import HttpResponse
from models import *

# Index view with a request, and a render response to index.html


def index(request):
	return render(request, 'index.html')


def flights(request, departure=None):
	context = {}

	if departure:
		context['departure'] = departure
		context['flights'] = Flight.objects.filter(location_departure=departure)
	else:
		context['flights'] = Flight.objects.all()

	return render(request, 'flights.html', context)
