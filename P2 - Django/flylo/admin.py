from django.contrib import admin
from models import Flight, Airline, Airplane, Reservation, Client, FlightOwner


def column_lister(model):
	""" Creates a new ModelAdmin that always lists the models in a table fashion. """

	class ListAdmin(admin.ModelAdmin):
		list_display = [f.name for f in model._meta.fields]

	return ListAdmin


class FlightsAdmin(admin.ModelAdmin):
	list_display = [f.name for f in Flight._meta.fields]

	def get_queryset(self, request):
		""" Show commercials only the Flights they created. """
		qs = super(FlightsAdmin, self).get_queryset(request)

		# Show all for SuperUser
		if request.user.is_superuser:
			return qs

		# Filter by owner if Staff
		own_flights = FlightOwner.objects.filter(owner=request.user)
		own_flights = [fo.flight.pk for fo in own_flights]
		return qs.filter(pk__in=own_flights)

	def save_model(self, request, obj, form, change):
		""" Model Save Hook to record Flight Owner """
		super(FlightsAdmin, self).save_model(request, obj, form, change)
		if request.user.is_superuser:
			return

		own_flights = FlightOwner.objects.filter(owner=request.user, flight=obj)
		if own_flights.count() < 1:
			new_fo = FlightOwner(owner=request.user, flight=obj)
			new_fo.save()




admin.site.register(Flight, FlightsAdmin)
admin.site.register(Airline, column_lister(Airline))
admin.site.register(Airplane, column_lister(Airplane))
admin.site.register(Reservation, column_lister(Reservation))
admin.site.register(Client, column_lister(Client))
admin.site.register(FlightOwner, column_lister(FlightOwner))
