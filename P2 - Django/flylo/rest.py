from rest_framework import viewsets, permissions
from rest_framework.response import Response
from serializers import FlightSerializer, AirlineSerializer, AirplaneSerializer

from flylo.models import Flight, FlightOwner, Airplane, Airline, User


class IsFlightCreatorOrReadOnly(permissions.BasePermission):
	def has_permission(self, request, view):
		""" Global query permissions """
		if request.method in permissions.SAFE_METHODS:
			return True
		else:
			return request.user.is_staff

	def has_object_permission(self, request, view, obj):
		# Read permissions are allowed to any request,
		# so we'll always allow GET, HEAD or OPTIONS requests.
		if request.method in permissions.SAFE_METHODS:
			return True

		for owner in FlightOwner.objects.all():
			if owner.flight == obj and owner.owner == request.user:
				return True

		return False


class FlightViewSet(viewsets.ModelViewSet):
	serializer_class = FlightSerializer
	queryset = Flight.objects.all()

	permission_classes = (IsFlightCreatorOrReadOnly,)

	def create(self, request, *args, **kwargs):
		request.data._mutable = True

		# TODO THIS ---- DOES NOT ----ING WORK

		# Airplane lookup
		apk = request.POST['airplane']
		request.data['airplane'] = AirplaneSerializer(Airplane.objects.get(pk=apk))

		# Airline Lookup
		alpks = [p for p in request.POST.get('airlines[]')]
		airlines = Airline.objects.filter(pk__in=alpks)
		als = AirlineSerializer(airlines, many=True)
		request.data['airlines'] = als

		raise Exception(request.data)

		super(FlightViewSet, self).create(request, args, kwargs)

	def perform_create(self, serializer):
		""" Override Object creation method to save Flight Owner record. """
		super(FlightViewSet, self).perform_create(serializer)

		owner = User.objects.get(username=self.request.user.username)
		f = Flight.objects.get(flight_number=serializer.data['flight_number'])
		FlightOwner.objects.create(owner=owner, flight=f)

	def put(self, request, *args, **kwargs):
		super(FlightViewSet, self).update(request, args, kwargs)

	def get_queryset(self):
		""" Override Get Queryset method to implement custom filtering. """
		from datetime import datetime
		queryset = Flight.objects.all()

		dep = self.request.query_params.get('departure', None)
		arr = self.request.query_params.get('arrival', None)
		dept = self.request.query_params.get('departure_time', None)
		arrt = self.request.query_params.get('arrival_time', None)

		if dep:
			queryset = queryset.filter(location_departure=dep)
		if arr:
			queryset = queryset.filter(location_arrival=arr)
		if dept:
			dept = datetime.strptime(dept, "%Y-%m-%dT%H:%M:%SZ")
			queryset = queryset.filter(estimated_time_departure__ge=dept)
		if arrt:
			arrt = datetime.strptime(dept, "%Y-%m-%dT%H:%M:%SZ")
			queryset = queryset.filter(estimated_time_arrival__ge=arrt)

		return queryset
