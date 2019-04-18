import numpy as np
import matplotlib.pyplot as plt

f = open("movement_plot.csv", "r")
csvRows = f.readlines()

#nodes = [
#			[nodeId,nodeX,nodeY,nodeDest],
#			[nodeId,nodeX,nodeY,nodeDest],
#			...
#		]

nodes = []

#Line Structure:
# time:nodeId,nodeX,nodeY,nodeDest;nodeId,nodeX,nodeY,nodeDest;...;

#Extract node information and store in the nodes array
print("--- Loading Data ---")
for row in csvRows:
	#print("row=" + str(row))
	#Split the row on time delimiter ":"
	timeSplit = row.rstrip().split(":")

	#Split timeSplit[1] on node delimiter ";"
	nodeSplit = timeSplit[1].split(";")

	rowNodeInformation = []
	for nodeString in nodeSplit:
		#Split on information delimiter ","
		rowNodeInformation.append(nodeString.split(","))

	nodes.append(rowNodeInformation)

f.close()


#Separate out all points X & Y, per row...
#
# lines = [
#			[[x1,x2,x3,...],[y1,y2,y3,...]],
#			[[x1,x2,x3,...],[y1,y2,y3,...]],
#			...
#		  ]
lines = {}

#Initialize the lines dictionary...
for node in nodes[0]:
	if(len(node[0]) > 0):
		lines[node[0]] = []

#print("Lines Keys: " + str(lines.keys()))

#Initialize the lines within the lines dictinary to have two arrays, one for X's and one for Y's
for key in lines.keys():
	lines[key] = {}
	lines[key]['x'] = []
	lines[key]['y'] = []
	
#print("Lines " + str(lines))

#Separate out the X's and Y's
for instance in nodes:
	for node in instance:
		if len(node) == 4:
			nodeId = node[0]
			lines[nodeId]['x'].append(float(node[1]))
			lines[nodeId]['y'].append(float(node[2]))

colors = {}
for key in lines.keys():
	print(key)
	colors[int(key)] = np.random.rand(3,1)

#Now that we have our node information separated out, plot it...
print("--- Starting Plotting ---")
lineCounter = 0
for line in lines.values():
	plt.plot(line['x'], line['y'], colors[lineCounter])
	lineCounter = lineCounter + 1

#Set plot settings and show the plot
plt.ylabel('Y')
plt.xlabel('X')
print("--- Plotting Finished ---")
plt.draw()
plt.show()
