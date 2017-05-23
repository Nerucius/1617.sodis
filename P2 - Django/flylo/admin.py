from django.contrib import admin
from models import *


def column_lister(model):
	""" Creates a new ModelAdmin that always lists the models in a table fashion. """

	class ListAdmin(admin.ModelAdmin):
		list_display = [f.name for f in model._meta.fields]

	return ListAdmin


admin.site.register(Flight, column_lister(Flight))
admin.site.register(Airline, column_lister(Airline))
admin.site.register(Airplane, column_lister(Airplane))
admin.site.register(Reservation, column_lister(Reservation))
admin.site.register(Client, column_lister(Client))
admin.site.register(FlightOwner, column_lister(FlightOwner))
