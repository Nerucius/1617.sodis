from time import time
from datetime import datetime
from random import randint, choice
import json


def main():
	locs = ['BCN', 'MAD', 'LAX', 'HKG', 'FRA', 'CAN', 'ORD', 'DEN', 'TKK', 'ATL', 'PEK']
	status = ['CANCELLED', 'FLYING', 'GATE', 'BOARDING', 'OK', 'DELAYED', 'ON TIME']
	airlines = ['ANB', 'DAL', 'CAL', 'IBE', 'VLG', 'RYA']
	planes = ['B747', 'A234', 'B787', 'B737', 'A230']

	now = int(time())

	file = open("flights2.json", "w")

	file.write("[")

	for i in range(20):
		# Departure and arrival times
		dep = randint(now, now + 4 * 3600)
		arr = dep + randint(3, 5) * 3600
		departure = datetime.fromtimestamp(dep)
		arrival = datetime.fromtimestamp(arr)
		departure = "{0}+0000".format(departure)
		arrival = "{0}+0000".format(arrival)

		a1 = choice(locs)
		a2 = choice(locs)
		if a1 == a2:
			i = i - 1
			continue

		airline = choice(airlines)
		fn = "{}{}".format(airline, randint(1001, 9999))

		flight = {}
		flight["model"] = "flylo.Flight"
		flight["pk"] = i + 1
		flight["fields"] = {}
		flight["fields"]["flight_number"] = fn
		flight["fields"]["estimated_time_departure"] = departure
		flight["fields"]["estimated_time_arrival"] = arrival
		flight["fields"]["location_departure"] = a1
		flight["fields"]["location_arrival"] = a2
		flight["fields"]["airline"] = airline
		flight["fields"]["aircraft"] = choice(planes)
		flight["fields"]["status"] = choice(status)

		"""{
			"model": "flylo.Flight",
			"pk": {},
			"fields": {
				"flight_number": "{}",
				"estimated_time_departure": "{}",
				"estimated_time_arrival": "{}",
				"location_departure": "{}",
				"location_arrival": "{}",
				"airline": "{}",
				"aircraft": "{}",
				"status": "{}"
			}
		},"""

		jstr = json.dumps(flight, sort_keys=False, indent=4, separators=(',', ': '))
		file.write(jstr)
		file.write(",")

	file.write("]")


if __name__ == "__main__":
	main()
