/**
 * dmLabLib, a Library created for use in various projects at the Data Mining Lab  
 * (http://dmlab.cs.gsu.edu/) of Georgia State University (http://www.gsu.edu/).  
 *  
 * Copyright (C) 2019 Georgia State University
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation version 3.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.gsu.cs.dmlab.tracking.interfaces;

import java.util.List;

import edu.gsu.cs.dmlab.datatypes.interfaces.ISTTrackingTrajectory;

/**
 * This is the interface of objects that solve the association problem of
 * tracks. From Kempton et. al
 * <a href="http://dx.doi.org/10.1016/j.ascom.2015.10.005">2015</a> and
 * <a href="https://doi.org/10.3847/1538-4357/aae9e9"> 2018</a>.
 * 
 * @author Dustin Kempton, Data Mining Lab, Georgia State University
 * 
 */
public interface ISTAssociationProblem {
	/**
	 * Returns the tracks from the solved association problem.
	 * 
	 * @return The tracks that were the result of the association problem.
	 */
	public List<ISTTrackingTrajectory> getTrackLinked();

	/**
	 * Adds a possible path to the association problem.
	 * 
	 * @param from The ITrack the path starts from.
	 * @param to   The ITrack the path ends at.
	 * 
	 */
	public void addAssociationPossibility(ISTTrackingTrajectory from, ISTTrackingTrajectory to);

	/**
	 * Solves the association problem
	 * 
	 * @return True if the problem was solved.
	 */
	public boolean solve();
}
