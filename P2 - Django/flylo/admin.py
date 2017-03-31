from django.contrib import admin
from models import *

# Register your models here.

class FlightAdmin(admin.ModelAdmin):
    list_display = [f.name for f in Flight._meta.fields]

    def __unicode__(self):
        """ Function that returns the table display columns for Django-Admin. """
        return self.list_display


admin.site.register(Flight, FlightAdmin)