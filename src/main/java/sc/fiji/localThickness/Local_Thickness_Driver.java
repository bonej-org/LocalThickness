/*
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2006 - 2020 Fiji developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package sc.fiji.localThickness;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/* Bob Dougherty 8/10/2007
Perform all of the steps for the local thickness calculaton


 License:
	Copyright (c) 2007, OptiNav, Inc.
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:

		Redistributions of source code must retain the above copyright
	notice, this list of conditions and the following disclaimer.
		Redistributions in binary form must reproduce the above copyright
	notice, this list of conditions and the following disclaimer in the
	documentation and/or other materials provided with the distribution.
		Neither the name of OptiNav, Inc. nor the names of its contributors
	may be used to endorse or promote products derived from this software
	without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
	"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
	LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
	A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
	CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
	EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
	PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
	PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
	LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
	NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
public class Local_Thickness_Driver implements PlugInFilter {

	private ImagePlus imp;
	public int thresh;
	public boolean inverse;

	@Override
	public int setup(final String arg, final ImagePlus imp) {
		this.imp = imp;
		return DOES_8G;
	}

	@Override
	public void run(final ImageProcessor ip) {
		final String title = stripExtension(imp.getTitle());
		imp.unlock();
		if (!getScale()) return;
		if (inverse) {
			IJ.run("Geometry to Distance Map", "threshold=" + thresh + " inverse");
		}
		else {
			IJ.run("Geometry to Distance Map", "threshold=" + thresh);
		}
		final ImagePlus impDM = WindowManager.getCurrentImage();
		IJ.run("Distance Map to Distance Ridge");
		final ImagePlus impDR = WindowManager.getCurrentImage();
		impDM.hide();
		impDM.flush();
		WindowManager.setTempCurrentImage(impDR);
		IJ.run("Distance Ridge to Local Thickness");
		final ImagePlus impLT = WindowManager.getCurrentImage();
		impDR.hide();
		impDR.flush();
		IJ.run("Local Thickness to Cleaned-Up Local Thickness");
		final ImagePlus impLTC = WindowManager.getCurrentImage();
		impLT.hide();
		impLT.flush();
		impLTC.setTitle(title + "_LocThk");
		IJ.showProgress(1.0);
		IJ.showStatus("Done");
	}

	// Modified from ImageJ code by Wayne Rasband
	static String stripExtension(String name) {
		if (name != null) {
			final int dotIndex = name.lastIndexOf(".");
			if (dotIndex >= 0) name = name.substring(0, dotIndex);
		}
		return name;
	}

	boolean getScale() {
		thresh = (int) Prefs.get("edtS1.thresh", 128);
		inverse = Prefs.get("edtS1.inverse", false);
		final GenericDialog gd = new GenericDialog("EDT...", IJ.getInstance());
		gd.addNumericField("Threshold (1 to 255; value < thresh is background)",
			thresh, 0);
		gd.addCheckbox("Inverse case (background when value >= thresh)", inverse);
		gd.showDialog();
		if (gd.wasCanceled()) return false;
		thresh = (int) gd.getNextNumber();
		inverse = gd.getNextBoolean();
		Prefs.set("edtS1.thresh", thresh);
		Prefs.set("edtS1.inverse", inverse);
		return true;
	}
}
