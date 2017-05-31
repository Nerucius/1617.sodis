# -*- coding: utf-8 -*-
# Generated by Django 1.11.1 on 2017-05-23 14:10
from __future__ import unicode_literals

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('flylo', '0008_flightowner'),
    ]

    operations = [
        migrations.AlterField(
            model_name='flightowner',
            name='flight',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.CASCADE, to='flylo.Flight'),
        ),
        migrations.AlterField(
            model_name='flightowner',
            name='owner',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL),
        ),
    ]