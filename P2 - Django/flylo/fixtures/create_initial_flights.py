from time import time
from datetime import datetime
from random import randint, choice
import json


def main():
	num_flights = 12
	now = int(time())

	locations = ['BCN', 'SXF', 'ZRH']
	status = ['BOARDING', 'DELAYED', 'ON TIME', 'LANDED', 'CANCELLED']

	# read airlines json data
	airlines_json_data = open("airlines.json").read()
	airlines_data = json.loads(airlines_json_data)
	num_airlines = len(airlines_data)

	# read airplanes json data
	airplanes_json_data = open("airplanes.json").read()
	airplanes_data = json.loads(airplanes_json_data)
	num_airplanes = len(airplanes_data)

	# open a flights file to write the generated flights in it
	file_flights = open("flights.json", "w")
	file_flights.write("[")

	for i in range(num_flights):

		# Departure and arrival times
		#starting 30 days in the future, up to 60
		spd = 24 * 60 * 60
		spm = 30 * spd
		time_departure = randint(now + 30*spm, now + 60*spm)
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
		for i in range(randint(1, num_airlines)):
			airlines.append(choice(airlines_data[i]))

		# Airplane
		airplane = airplanes_data[randint(0, num_airplanes-1)]

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
		flight["fields"]["airline"] = [a['pk'] for a in airlines]			# assign an airline through the foreign key
		flight["fields"]["airplane"] = airplane["pk"]		# assign an airplane through the foreign key
		flight["fields"]["status"] = choice(status)

		json_str = json.dumps(flight, sort_keys=False, indent=4, separators=(',', ': '))
		file_flights.write(json_str)
		file_flights.write(",")

	file_flights.write("]")


if __name__ == "__main__":
	main()
