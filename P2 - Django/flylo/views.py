from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.core.urlresolvers import reverse

from .forms import *

# Flags to control which flight view has to be rendered
SIMPLE_FLIGHTS = False
DETAILED_FLIGHTS = False


# Index view with a request, and a render response to index.html
def index(request):
	context = {}
	# In case the form has been completed, redirect to the filtered by departure flights page
	if request.method == 'POST':
		form = SelectDepartureForm(request.POST)
		if form.is_valid():
			id_departure = form.cleaned_data.get('departure')
			departure = form.get_departure(id_departure)
			return HttpResponseRedirect('flights/' + departure)

	# If not, send the empty form
	else:
		context['form'] = SelectDepartureForm()
	return render(request, 'flylo/index.html', context)


def flights(request, departure=None):
	context = {}

	if departure:
		context['departure'] = departure
		context['flights'] = Flight.objects.filter(location_departure=departure)
	else:
		context['flights'] = Flight.objects.all()

	if SIMPLE_FLIGHTS:
		template = 'flylo/simple_flights.html'
	elif DETAILED_FLIGHTS:
		template = 'flylo/detailed_flights.html'
	else:
		template = 'flylo/flights.html'

	return render(request, template, context)


def shoppingcart(request):
	selectedFlights = []
	for key in request.POST:
		if key.startswith("checkbox"):
			selectedFlights.append(request.POST[key])
	request.session["selectedFlights"] = selectedFlights
	return HttpResponseRedirect(reverse('flylo:buy'))


def buy(request):
	return HttpResponse("TODO buy view")