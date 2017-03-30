from django.contrib import admin
from models import *

# Register your models here.

class FlightAdmin(admin.ModelAdmin):
    pass


admin.site.register(Flight, FlightAdmin)