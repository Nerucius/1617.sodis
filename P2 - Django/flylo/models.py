from __future__ import unicode_literals

from django.db import models

# Create your models here.

class Flight(models.Model):

    flight_number = models.CharField(max_length=8)
    estimated_time_departure = models.DateTimeField()
    estimated_time_arrival = models.DateTimeField()
    location_departure = models.CharField(max_length=3)
    location_arrival = models.CharField(max_length=3)
    airline = models.CharField(max_length=3)
    aircraft = models.CharField(max_length=4)
    status = models.CharField(max_length=40)

    class Meta:
        ordering = ['airline', 'id']

    def __str__(self):
        return "[" + self.flight_number + "] " + self.location_departure + "-" + self.location_arrival + " : " + str(
            self.estimated_time_departure) + " / " + str(self.estimated_time_arrival) + " [" + self.status + "]"
