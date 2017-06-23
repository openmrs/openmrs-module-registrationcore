<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<spring:htmlEscape defaultHtmlEscape="true" />
<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/index.htm" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />


<h2>HIE Patient Search</h2>
<div>
	<b class="boxHeader">Find Patients</b>
	<div class="box">
		This will search the Health Information Exchange for patients matching your query parameters. Note, the results from this result may not exist in your local OpenMRS instance but can be imported.
		<form id="importForm" modelAttribute="patientSearch" method="post"
			enctype="multipart/form-data">

			<table>
				<tr>
					<td>Family Name:</td>
					<td><input type="text" name="familyName" value="${patientSearch.familyName }" /></td>
					<td>Given Name:</td>
					<td><input type="text" name="givenName"  value="${patientSearch.givenName }"/></td>
					<td>Identifier</td>
					<td colspan="3"><input type="text" name="identifier" value="${patientSearch.identifier }"/>
				</tr>
			</table>
			<br /> <input type="submit" value="Search"> <br />
		</form>
	</div>

	<!-- Display results in a simple table -->
	<c:if test="${hasResults}">
		<table style="width:100%">
			<tr>
				<th>ID</th>
				<th>Family Name</th>
				<th>Given Name</th>
				<th>Date Of Birth</th>
				<th>Gender</th>
				<th>Action</th>
			</tr>
			<c:forEach var="patient" items="${results}">
				<tr>
					<td style="border-bottom:solid 1px #ddd">${patient.familyName }</td>
					<td style="border-bottom:solid 1px #ddd">${patient.givenName }</td>
					<td style="border-bottom:solid 1px #ddd">
						<c:choose>
							<c:when test="${patient.isImported }">
								<c:url var="viewPatientUrl" value="/patientDashboard.form"/>
								<a href="${viewPatientUrl }?patientId=${patient.openMrsId }">View</a>
							</c:when>
							<c:otherwise>
								<c:url var="importPatientUrl" value="/module/openhie-client/hieImportPatient.form"/>
								<a href="${importPatientUrl }?ecid=${patient.ecid}">Import</a>
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:forEach>
		</table>
	</c:if>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
