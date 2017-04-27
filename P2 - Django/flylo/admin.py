from django.contrib import admin
from models import *

# Register your models here.

"""
class ListAdmin(admin.ModelAdmin):

    def __unicode__(self):
        Function that returns the table display columns for Django-Admin.
        list_display = [f.name for f in self.model._meta.fields]
        return list_display
"""

admin.site.register(Flight)
admin.site.register(Airline)
admin.site.register(Airplane)
