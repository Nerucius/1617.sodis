from django.test import TestCase


class APIPostFlightTest(TestCase):
	""" Test Case for Commercial posting a new Flight"""
	serialized_rollback = True
	user = None

	def setUp(self):
		# Load initial data
		from django.core.management import call_command
		call_command("loaddata", "initial_data.json")

		user_cred = {
			'username': 'juanjo',
			'password': 'password123'
		}

		self.client.post('http://localhost:8080/flylo/login/', user_cred)

	def test_commercial_flight_post(self):
		from datetime import datetime

		nf = {
			'pk': 999,
			'flight_number': 'TST999',
			'estimated_time_arrival': datetime.now(),
			'estimated_time_departure': datetime.now(),
			'location_departure': 'TST',
			'location_arrival': 'TST',
			'status': 'TESTING',
			'airplane': 1,
			'airlines': [1, 2]
		}

		r = self.client.post('/api/flights/', nf)
		# r = self.client.get('/api/flights/?departure=TST')
		raise Exception(r)

	def test_commercial_flight_update(self):
		nf = {
			'flight_number': 'TST0000',
		}

		self.client.put('/api/flights/999', nf)
		r = self.client.get('/api/flights/?departure=TST')

		raise Exception(r)
