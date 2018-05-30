/*
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2016 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 6. The University of California, San Diego, La Jolla, CA, USA
 * 7. The Babraham Institute, Cambridge, UK
 * 8. Duke University, Durham, NC, US
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.omex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import org.jdom2.JDOMException;

import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;

/**
 * @author Shalin
 * @since 1.5
 */
public class OMEXArchive {

	private CombineArchive archive;

	public OMEXArchive(File zipFile) throws IOException, ParseException, CombineArchiveException, JDOMException{
		archive = new CombineArchive(zipFile);

		// read description of the archive itself
		System.out.println("found " + archive.getDescriptions().size() + " meta data entries describing the archive.");
	
		// iterate over all entries in the archive
		for (ArchiveEntry entry : archive.getEntries())
		{
			// display some information about the archive
			System.out.println(">>> file name in archive: " + entry.getFileName() + "  -- apparently of format: " + entry.getFormat());

			// We want to read it, you do not need to extract it
			// so we call for an InputStream:
			try {
				InputStream myReader = Files.newInputStream (entry.getPath(), StandardOpenOption.READ);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Cannnot read zip archive.");
				return;
			}
		}
	}

	/**
	 * A simple method to uncompress combine archives
	 * @param File
	 * @return boolean
	 */
	public boolean extractArchive(File destination) {
		try {
			// Extract the whole archive to our disk at the specified location
			archive.extractTo(destination);
			return true;
		}catch(IOException ex) {
			return false;
		}
	}
}