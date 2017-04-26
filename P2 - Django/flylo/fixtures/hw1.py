from time import time
from datetime import datetime
from random import randint, choice
import json

def main():
	locations = ['BCN', 'SXF', 'ZRH']
	status = ['CANCELLED', 'FLYING', 'GATE', 'BOARDING', 'OK', 'DELAYED', 'ON TIME']
	airlines = ['ANB', 'DAL', 'CAL', 'IBE', 'VLG', 'RYA']
	planes = ['B747', 'A234', 'B787', 'B737', 'A230']

	now = int(time())

	file = open("hw1_flights.json", "w")

	file.write("[")

	for i in range(12):

		# Departure and arrival times
		departure = randint(now, now + 4 * 3600)
		arrival = departure + randint(3, 5) * 3600

		departure = datetime.fromtimestamp(departure)
		arrival = datetime.fromtimestamp(arrival)

		departure = "{0}+0200".format(departure)
		arrival = "{0}+0200".format(arrival)

		# Departure and arrival locations
		departure_location = choice(locations)
		other_locations = list(locations)
		other_locations.remove(departure_location)
		arrival_location = choice(other_locations)

		# Airline
		airline = choice(airlines)

		# Flight number
		fn = "{}{}".format(airline, randint(1001, 9999))

		flight = {}
		flight["model"] = "flylo.Flight"
		flight["pk"] = i + 1
		flight["fields"] = {}
		flight["fields"]["flight_number"] = fn
		flight["fields"]["estimated_time_departure"] = departure
		flight["fields"]["estimated_time_arrival"] = arrival
		flight["fields"]["location_departure"] = departure_location
		flight["fields"]["location_arrival"] = arrival_location
		flight["fields"]["airline"] = airline
		flight["fields"]["aircraft"] = choice(planes)
		flight["fields"]["status"] = choice(status)

		jstr = json.dumps(flight, sort_keys=False, indent=4, separators=(',', ': '))
		file.write(jstr)
		file.write(",")

	file.write("]")


if __name__ == "__main__":
	main()
