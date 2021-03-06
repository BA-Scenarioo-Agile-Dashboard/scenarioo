/* scenarioo-server
 * Copyright (C) 2014, scenarioo.org Development Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.scenarioo.rest.base.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.scenarioo.api.exception.ResourceNotFoundException;


import com.sun.istack.logging.Logger;

@Provider
public class ExceptionHandler implements ExceptionMapper<ResourceNotFoundException> {
	
	private Logger LOGGER = Logger.getLogger(ExceptionHandler.class);
	
	@Override
	public Response toResponse(final ResourceNotFoundException exception) {
		LOGGER.info("Resource not found", exception);
		return Response.status(500).build();
	}
}
