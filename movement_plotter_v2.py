import math
import numpy as np
import matplotlib.pyplot as plt

f_base = open("base_station_coordinates.csv", "r")
data = f_base.readlines()


# Extract base stations
base_stations = {}

print("Base Stations:")

for line in data:
	split_line = line.rstrip().split(",")

	if len(split_line) == 3:
		station_id = split_line[0]
		station_x = round(float(split_line[1]))
		station_y = round(float(split_line[2]))
		base_stations[station_id] = {}
		base_stations[station_id]['x'] = station_x
		base_stations[station_id]['y'] = station_y

		print("base_station: " + station_id + ", " + str(station_x) + ", " + str(station_y))

print("\n")
print(base_stations)
print("\n")

f_base.close()

f_movement = open("Test.csv", "r")
#f_movement = open("dynamic_node_coordinates.csv", "r")
data = f_movement.readlines()

# Extract dynamic nodes
nodes = {}

print("Dynamic Nodes Extracted...")

for line in data:
	#Split on ":"
	semicolonSplit = line.rstrip().split(":")

	if len(semicolonSplit) == 2:
		#Split on ";"
		colonSplit = semicolonSplit[1].rstrip().split(";")

		for item in colonSplit:
			tempArray = []
			if len(item) > 0:
				tempArray = item.split(",")
				
				if len(tempArray) == 4:
					tempId = tempArray[0]
					tempX = round(float(tempArray[1]), 2)
					tempY = round(float(tempArray[2]), 2)

					if tempId in nodes.keys():
						nodes[tempId]['x'].append(tempX)
						nodes[tempId]['y'].append(tempY)
					else :
						nodes[tempId] = {}
						nodes[tempId]['x'] = [tempX]
						nodes[tempId]['y'] = [tempY]

f_movement.close()

# Merge nodes with base_stations
#for stationIndex in base_stations:
	#nodes[stationIndex] = base_stations[stationIndex]

	#print(nodes[stationIndex])


# Generate colors for plotting
colors = {}
for node in nodes:
	colors[node] = np.random.rand(3,1)

print("Plotting...")

#Parameters from simulation!
plotBaseStations = True
regionWidth = 300
regionHeight = 300
baseStationTransmissionRadius = 25

# Plot!
if plotBaseStations:
	for station in base_stations.keys():
		#print(station)
		plt.plot(base_stations[station]['x'], base_stations[station]['y'], "o", markersize=(2 * baseStationTransmissionRadius), alpha=0.5)

for node in nodes.values():
	plt.plot(node['x'], node['y'], '--')


plt.axis([0, 300, 0, 300])
plt.xlabel("X Axis")
plt.ylabel("Y Axis")
plt.show()




