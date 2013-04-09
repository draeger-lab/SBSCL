#
# Author:  Andreas Draeger
# Version: $Rev$
#
# Run this script in R by typing
# source("plot.R")

# Read data
X <- data.frame(read.csv("BIOMODEL206.txt"))
Y <- data.frame(read.csv("BIOMODEL390.txt"))

x <- data.matrix(X)
y <- data.matrix(Y)

bounds <- function(matrix, indices) {
  minx <- min(matrix[,1])
  maxx <- max(matrix[,1])
  miny <- max(matrix)
  maxy <- min(matrix)
  for (i in 1:length(indices)) {
    if (min(matrix[,indices[i]]) < miny) {
      miny <- min(matrix[,indices[i]])
    }
    if (max(matrix[,indices[i]]) > maxy) {
      maxy <- max(matrix[,indices[i]]) 
    }
  }
  res <- matrix(c(minx, maxx, miny, maxy), ncol=2, byrow=FALSE)
  res
}

plot_species <- function(b, indices, names, matrix) {
  plot(c(b[1,1], b[2,1]) * 60, c(b[1,2], b[2,2]), type="n", xlab="Time in s", ylab=expression(paste("Concentration in ", mmol%.%l^-1)))
  legend("topleft", names, col=2:(length(indices)+1), lty=1, lwd=2)
  for (i in 1:length(indices)) {
    lines(matrix[,1] * 60, matrix[,indices[i]], type="l", col=i+1, lty=1, lwd=2)
  }
  #title("Species in model 206")
}

plot_fluxes <- function(b, indices, names, matrix) {
  plot(c(b[1,1], b[2,1]) * 60, c(b[1,2], b[2,2]) / 60, type="n", xlab="Time in s", ylab=expression(paste("Flux in ", mmol%.%s^-1)))
  legend("topleft", names, col=2:(length(indices)+1), lty=1, lwd=2)
  for (i in 1:length(indices)) {
    lines(matrix[2:length(matrix[,1]),1] * 60, matrix[2:length(matrix[,indices[i]]),indices[i]] / 60, type="l", col=i+1, lty=1, lwd=2)
  }
  title("Fluxes in model 206")
}


# plot all
pdf("test.pdf", paper="a4r")
par(mfrow=c(3,3))
for (i in 1:length(x[1,])) {
  plot(x[,1], x[,i], type="l", col=i, xlab=colnames(x)[1], ylab=colnames(x)[i], lty=1, lwd=2)
  title(i)
}
dev.off()


############################################################################################

pdf("test.pdf", paper="a4r")
par(mfrow=c(2,2))

indices <- c(3, 4, 6, 7, 8)
names <- c("Glucose", "ATP", "Glyceraldehyde 3-phosphate", expression(NAD^"+"), "3-Phosphoglycerate")
b <- bounds(x, indices)
b[2,2] <- 3.3
plot_species(b, indices, names, x)


indices <- c(5, 9)
names <- c("Fructose 1,6-bisphosphate", "Pyruvate")
b <- bounds(x, indices)
plot_species(b, indices, names, x)

indices <- c(10, 11)
names <- c("Acetaldehyde", "extracellular acetaldehyde")
b <- bounds(x, indices)
plot_species(b, indices, names, x)

indices <- 30:39
b <- bounds(x, indices)
names <- c("D-glucose 6-phosphotransferase", "glycerone-phosphate-forming", "phosphoglycerate kinase", "pyruvate 2-O-phosphotransferase", "acetaldehyde-forming", "ATP biosynthetic process", "sn-Glycerol-1-phosphate:NAD 2-oxidoreductase", "acetaldehyde catabolic process", "v10", "ethanol:NAD+ oxidoreductase")
plot_fluxes(b, indices, names, x)

dev.off()

###### Figure 7a

plot_species <- function(b, indices, names, matrix) {
  plot(c(b[1,1], b[2,1]) * 60, c(b[1,2], b[2,2]), type="n", xlab="Time in s", ylab=expression(paste("Concentration in ", mmol%.%l^-1)))
  legend("topleft", names, col=2:(length(indices)+1), lty=1, lwd=2, ncol=2, bty="n")
  for (i in 1:length(indices)) {
    lines(matrix[,1] * 60, matrix[,indices[i]], type="l", col=i+1, lty=1, lwd=2)
  }
  #title("Species in model 206")
}


pdf("test.pdf", width=11.7, height=8.3) 
par(cex=2, mai=c(1.7, 1.7, 0.1, 0.1))
indices <- c(3, 4, 6, 7, 8)
names <- c("Glucose", "ATP", "Glyceraldehyde 3-phosphate", expression(NAD^"+"), "3-Phosphoglycerate")
b <- bounds(x, indices)
b[2,2] <- 3.6 #3.3
plot_species(b, indices, names, x)
dev.off()

###### Figure 7b

plot_fluxes <- function(b, indices, names, matrix) {
  plot(c(b[1,1], b[2,1]) * 60, c(b[1,2], b[2,2]) / 60, type="n", xlab="Time in s", ylab=expression(paste("Flux in ", mmol%.%s^-1)))
  legend("topleft", names, col=2:(length(indices)+1), lty=1, lwd=2, bty="n")
  for (i in 1:length(indices)) {
    lines(matrix[2:length(matrix[,1]),1] * 60, matrix[2:length(matrix[,indices[i]]),indices[i]] / 60, type="l", col=i+1, lty=1, lwd=2)
  }
}

pdf("test.pdf", width=11.7, height=8.3) 
par(cex=2, mai=c(1.7, 1.7, 0.1, 0.1))
indices <- c(30:35)
b <- bounds(x, indices)
names <- c("D-glucose 6-phosphotransferase", "glycerone-phosphate-forming", "phosphoglycerate kinase", "pyruvate 2-O-phosphotransferase", "acetaldehyde forming", "ATP biosynthetic process")
plot_fluxes(b, indices, names, x)
dev.off()


############################################################################################

###### Figure 8

indices <- c(4, 9, 10)
names <- c("Ribulose 1,5 bisphosphate", "ATP", "ADP")
b <- bounds(y, indices)
plot_species <- function(b, indices, names, matrix) {
  plot(c(b[1,1], b[2,1]), c(b[1,2], b[2,2]), type="n", xlab="Time in s", ylab=expression(paste("Concentration in ", mmol%.%l^-1)))
  legend("topright", names, col=2:(length(indices)+1), lty=1, lwd=2, bty="n")
  for (i in 1:length(indices)) {
    lines(matrix[,1], matrix[,indices[i]], type="l", col=i+1, lty=1, lwd=2)
  }
  #title("Species in model 206")
}

pdf("test.pdf", width=11.7, height=8.3) 
par(cex=2, mai=c(1.7, 1.7, 0.1, 0.1))
plot_species(b, indices, names, y)
dev.off()


############################################################################################

par(cex.axis=1.2, cex.lab=1.5, cex.main=2, cex.sub=2, omi=c(0, 0.4, 0, 0.4), mfrow=c(2, 2))
plot(x[,1], x[,3], type="l", col=2, xlab=colnames(x)[1], ylab=colnames(x)[3])
dev.off()

minx <- min(x[,1])
maxx <- max(x[,1])
miny <- max(x)
maxy <- min(x)
for (i in 1:length(indices)) { 
  print(indices[i])
    if (min(x[,indices[i]]) < miny) {
      miny <- min(x[,indices[i]])
    }
    if (max(x[,indices[i]]) > maxy) {
      maxy <- max(x[,indices[i]]) 
    }
}
plot(c(minx, maxx), c(miny, maxy), type="n", xlab=colnames(x)[1], ylab="Concentration")
for (i in 1:length(indices)) {
  lines(x[,1], x[,indices[i]], type="l", col=i, xlab=colnames(x)[1], ylab=colnames(x)[indices[i]], lty=1, lwd=2)
}
title("Species in model 206")
legend("topleft", colnames(x)[indices], col=1:length(indices), lty=1, lwd=2)
dev.off()




#par(mfrow=c(3,3))
# length(x[1,])

  #if ((i < 12) || (29 < i)) {
    #plot(x[,1], x[,i], type="l", col=i, xlab=colnames(x)[1], ylab=colnames(x)[i], lty=1, lwd=2)
    #title(i)