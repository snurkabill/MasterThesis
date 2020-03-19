import matplotlib
import matplotlib.pyplot as plt
from matplotlib.pyplot import show 
import matplotlib.image as mpimg 
from matplotlib.colors import ListedColormap
import seaborn as sns
import numpy as np
import pandas as pd
from sys import argv


# backgroundWithSteps = True: output = colored hallway map with steps count
# backgroundWithSteps = False: output = heatmap with steps (no background)
backgroundWithSteps = True

# path to the folder
folder = argv[1]


# finds out a hallway size
def count_hallway_size(dataset):
	hallwayInstance = f"{folder}\\hallwayInstance"
	m = 0
	with open(hallwayInstance) as f:
		for count, line in enumerate(f):
			if count == 0:
				n = line.count('W')
			if line[0] == 'W':
				m += 1
			else:
				continue
	f.close()
	return (m, n)


# creates an empty matrix as a hallway background
# creates an empty matrix to store number of steps 
def create_empty_matrices(m, n):
	hallwayImgZeros = np.zeros((m, n), dtype=int)
	stepsCountZeros = np.zeros((m, n), dtype=int)
	return (hallwayImgZeros, stepsCountZeros)


# reads a hallway and turns its structure to a matrix with numbers   
def create_background_matrix(dataset, hallwayImgZeros):
	hallwayInstance = f"{folder}\\hallwayInstance"
	hallwayImg = hallwayImgZeros
	with open(hallwayInstance) as f:
		for countLine, line in enumerate(f):
			for countLettre, letter in enumerate(str(line)):
				if countLettre % 2 == 0:
					if letter == "W":
						hallwayImg[countLine][int(countLettre / 2)] = 1
					elif letter == " ":
						hallwayImg[countLine][int(countLettre / 2)] = 2
					elif letter == "A":
						start = [countLine, int(countLettre / 2)]
						hallwayImg[countLine][int(countLettre / 2)] = 7
					elif letter == "X":
						hallwayImg[countLine][int(countLettre / 2)] = 0
					elif letter == "G":
						hallwayImg[countLine][int(countLettre / 2)] = 4
				else:
					continue
	f.close()
	return (hallwayImg, start)


# counts steps of robot in hallway
def count_steps(dataset, stepsCountZeros):
	stepsCount = stepsCountZeros
	for y in range (0, 1000): 
		stateDump = pd.read_csv(f"{folder}\\evaluation\\episodeId_{y}\\stateDump")
		for j in range (0, len(stateDump)): 
			if stateDump["Is agent turn"][j] == True:
				a = int(stateDump["Agent coordinate X"][j]) 
				b = int(stateDump["Agent coordinate Y"][j])	
				stepsCount[a][b] += 1
			else:
				continue 
		stepsCount[start[0]][start[1]] = stepsCount[start[0]][start[1]] - 1
	return stepsCount


# plots the hallway
def plot_hallway(matrix, plot):
	plot.imshow(matrix,
		cmap="Set1",
        aspect = plot.get_aspect(),
        extent = plot.get_xlim() + plot.get_ylim(),
        zorder = 1)


# plots number of steps as an annotated heatmap
def plot_steps(matrix):

	if backgroundWithSteps == True:
		cbarValue = False
		linecolorValue = "white"
		cmapValue = ListedColormap(['white'])
		alphaValue = 0.3
	else:
		cbarValue = True
		linecolorValue = "grey"
		cmapValue = "Greys"
		alphaValue = 1

	plot = sns.heatmap(matrix,
		annot=True, 
		fmt="d",
		linewidths=".5",
		cbar=cbarValue,
		cmap=cmapValue,
		alpha=alphaValue,
		linecolor=linecolorValue,
		zorder = 2)
	return plot


m = count_hallway_size(folder)[0]
n = count_hallway_size(folder)[1]

hallwayImgZeros = create_empty_matrices(m, n)[0]
stepsCountZeros = create_empty_matrices(m, n)[1]

hallwayImg = create_background_matrix(folder, hallwayImgZeros)[0]
start = create_background_matrix(folder, hallwayImgZeros)[1]

stepsCount = count_steps(folder, stepsCountZeros)

plot = plot_steps(stepsCount)

if backgroundWithSteps == True:
	plot_hallway(hallwayImg, plot)

plt.title(f"dataset: {folder}")	

show()