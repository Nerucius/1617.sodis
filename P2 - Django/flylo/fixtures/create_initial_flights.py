from __future__ import unicode_literals

from time import time
from datetime import datetime
import random
from random import randint, choice
import json


def main():
	num_flights = 12
	now = int(time())

	locations = ['BCN', 'SXF', 'ZRH']
	status = ['BOARDING', 'DELAYED', 'ON TIME', 'CANCELLED']

	# read airlines json data
	airlines_json_data = open("airlines.json").read()
	airlines_data = json.loads(airlines_json_data)
	num_airlines = len(airlines_data)

	# read airplanes json data
	airplanes_json_data = open("airplanes.json").read()
	airplanes_data = json.loads(airplanes_json_data)

	# open a flights file to write the generated flights in it
	file_flights = open("flights.json", "w")
	file_flights.write("[")

	for i in range(num_flights):

		# Departure and arrival times
		# starting 15 days in the future, up to 60
		spd = 24 * 60 * 60
		time_departure = randint(now + 15 * spd, now + 60 * spd)
		time_arrival = time_departure + randint(3, 6) * 3600

		time_departure = datetime.fromtimestamp(time_departure)
		time_arrival = datetime.fromtimestamp(time_arrival)

		time_departure = "{0}+0200".format(time_departure)
		time_arrival = "{0}+0200".format(time_arrival)

		# Departure and arrival locations
		location_departure = choice(locations)
		other_locations = list(locations)
		other_locations.remove(location_departure)
		location_arrival = choice(other_locations)

		# Airline
		airlines = []
		for j in range(randint(1, num_airlines - 1)):
			airlines.append(choice(airlines_data))

		# Airplane
		airplane = choice(airplanes_data)

		# Flight number
		fn = "{}{}".format(airlines[0]["fields"]["code"], randint(1001, 9999))

		flight = {}
		flight["model"] = "flylo.Flight"
		flight["pk"] = i + 1
		flight["fields"] = {}
		flight["fields"]["flight_number"] = fn
		flight["fields"]["estimated_time_departure"] = time_departure
		flight["fields"]["estimated_time_arrival"] = time_arrival
		flight["fields"]["location_departure"] = location_departure
		flight["fields"]["location_arrival"] = location_arrival
		flight["fields"]["airlines"] = [a['pk'] for a in airlines]  # assign an airline through the foreign key
		flight["fields"]["airplane"] = airplane['pk']  # assign an airplane through the foreign key
		flight["fields"]["status"] = choice(status)
		flight["fields"]["base_price"] = random.randrange(50, 80, 5)

		json_str = json.dumps(flight, sort_keys=False, indent=4, separators=(',', ': '))
		file_flights.write(json_str)
		file_flights.write(",")

	# LAST TWO ARE THE NECESARY FLIGHTS FOR COMPARISON

	flight = {}
	flight["model"] = "flylo.Flight"
	flight["pk"] = 13
	flight["fields"] = {}
	flight["fields"]["flight_number"] = "VLG1053"
	flight["fields"]["estimated_time_departure"] = "2017-03-28 21:40+0200"
	flight["fields"]["estimated_time_arrival"] = "2017-03-28 22:45+0200"
	flight["fields"]["location_departure"] = "BCN"
	flight["fields"]["location_arrival"] = "MAD"
	flight["fields"]["airlines"] = [1]
	flight["fields"]["airplane"] = 2
	flight["fields"]["status"] = "ON TIME"
	flight["fields"]["base_price"] = random.randrange(50, 80, 5)
	json_str = json.dumps(flight, sort_keys=False, indent=4, separators=(',', ': '))
	file_flights.write(json_str)

	file_flights.write(",")

	flight = {}
	flight["model"] = "flylo.Flight"
	flight["pk"] = 14
	flight["fields"] = {}
	flight["fields"]["flight_number"] = "DAL477"
	flight["fields"]["estimated_time_departure"] = "2017-03-29 12:10+0200"
	flight["fields"]["estimated_time_arrival"] = "2017-03-29 14:08-0400"
	flight["fields"]["location_departure"] = "BCN"
	flight["fields"]["location_arrival"] = "JFK"
	flight["fields"]["airlines"] = [2]
	flight["fields"]["airplane"] = 1
	flight["fields"]["status"] = "DELAYED"
	flight["fields"]["base_price"] = random.randrange(50, 80, 5)
	json_str = json.dumps(flight, sort_keys=False, indent=4, separators=(',', ': '))
	file_flights.write(json_str)

	file_flights.write("]")


if __name__ == "__main__":
	main()
