package org.scenarioo.example.livingDoc;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.scenarioo.api.ScenarioDocuWriter;
import org.scenarioo.api.files.ScenarioDocuFiles;
import org.scenarioo.api.util.xml.ScenarioDocuXMLFileUtil;
import org.scenarioo.model.docu.entities.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.scenarioo.api.rules.CharacterChecker.checkIdentifier;

/**
 * A simple example generator to transform a file structure of markdown files and gherkin feature files
 * as well as other tests files into the new Scenarioo feature structure.
 */
public class LivingDocGenerator {

	private final ScenarioDocuFiles docuFiles;
	private final String branchName;
	private final String buildName;
	File targetPath;

	List<DocLinkConfig> docLinkConfigs = new ArrayList<>();

	private ScenarioDocuWriter docuWriter;

	public LivingDocGenerator(File targetPath, String branchName, String buildName) {
		this.targetPath = targetPath;
		this.docuWriter = new ScenarioDocuWriter(targetPath, branchName, buildName);

		this.docuFiles = new ScenarioDocuFiles(targetPath);
		this.branchName = branchName;
		this.buildName = buildName;

	}

	private static File getResourceFile(final String relativeResourcePath) throws URISyntaxException {
		URL url = LivingDocGenerator.class.getClassLoader().getResource(relativeResourcePath);
		return new File(url.toURI());
	}

	/**
	 * Store XML with meta info for this branch.
	 */
	public void saveBranchDescription(Branch branch) {
		docuWriter.saveBranchDescription(branch);
	}

	/**
	 * Store XML with meta information for the whole build.
	 */
	public void saveBuildDescription(Build build) {
		docuWriter.saveBuildDescription(build);
	}

	/**
	 * Enable the egenrator to generate links for files with adding one (or several) link configurations.
	 */
	public void addDocLinkConfig(DocLinkConfig linkConfig) {
		docLinkConfigs.add(linkConfig);
	}

	/**
	 * Generate feature structure from docs directory with feature files.
	 * Structure is derived from recursive directory structure.
	 * Also will al the content of the source docs directory be copied to `docs` folder inside the generated docu build.
	 * @param docFilesSourceDir the source directory with feature files to generate feature structure from
	 */
	public void generateFeaturesFromDocs(File docFilesSourceDir) {
		try {

			// 1. copy all docs to docs folder in target dir
			FileUtils.copyDirectory(docFilesSourceDir, docuWriter.getDocsDirectory());

			// 2. recursively go through all documents in docs folder and generate features
			generateFeatures(docFilesSourceDir, "");

			copySampleScenarios();

		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("IO Exception on writing feature structure", e);
		}
	}

	private void copySampleScenarios() throws URISyntaxException, IOException {
		File dir = docuFiles.getBuildDirectory(branchName, buildName);
		File res = getResourceFile("example/find_no_results");

		System.out.println(res);

		for (File file:dir.listFiles()) {
			if(file.isDirectory()){
				System.out.println(file);
				File f = new File(file.getAbsolutePath()+"/find_no_results");
				f.mkdirs();
				FileUtils.copyDirectory(res, f);
			}
		}
	}

	private List<Feature> generateFeatures(File directory, String basePath) {
		// Generate features for all sub files (except for README.md)
		List<Feature> features = new ArrayList<>();
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				Feature feature = generateFeatureForDirectory(file, basePath + file.getName() + "/");
				features.add(feature);
			} else if (file.getName().equals("README.md")) {
				// ignore it, those files are used to document current feature directory only
			} else {
				Feature feature = generateFeatureForFile(file, basePath + file.getName());
				features.add(feature);
			}
		}
		return features;
	}

	private Feature generateFeatureForDirectory(File directory, String basePath) {

		// Create feature
		String name = directory.getName();
		Feature feature = new Feature(makeReadable(name), "description not yet parsed");
		feature.setId(name);

		// Attach README.md, if available
		File readmeFile = new File(directory, "README.md");
		if (readmeFile.exists()) {
			feature.setDocumentation(generateDocFile(readmeFile, basePath + "README.md"));
		}

		// Child features
		List<Feature> childFeatures = generateFeatures(directory, basePath);
		List<String> childFeatureNames = new ArrayList<>();
		for (Feature childFeature : childFeatures) {
			childFeatureNames.add(childFeature.getId());
		}
		feature.setSubFeatureNames(childFeatureNames);

		// save
		saveFeature(feature);
		return feature;
	}

	public void saveFeature(final Feature feature) {

		if (feature.getMilestone() == null)
			feature.setMilestone("M - 1");

		checkIdentifier(feature.getId());
		File destCaseDir = getFeatureDirectory(feature.getId());
		createDirectoryIfNotYetExists(destCaseDir);
		File destCaseFile = docuFiles.getFeatureFile(branchName, buildName, feature.getId());
		ScenarioDocuXMLFileUtil.marshal(feature, destCaseFile);
	}
	private void createDirectoryIfNotYetExists(final File directory) {
		docuFiles.assertRootDirectoryExists();
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}
	private File getFeatureDirectory(final String featureName) {
		return docuFiles.getFeatureDirectory(branchName, buildName, featureName);
	}


	private Feature generateFeatureForFile(File file, String basePath) {
		String name = truncEnding(file.getName());
		Feature feature = new Feature(makeReadable(name), "description not yet parsed");
		feature.setId(name);
		defineFeatureDetailsFromFileContent(feature, file);
		if (file.getName().toLowerCase().endsWith(".md")) {
			feature.setDocumentation(generateDocFile(file, basePath));
		} else {
			feature.setSpecification(generateDocFile(file, basePath));
		}
		saveFeature(feature);
		return feature;
	}

	private DocFile generateDocFile(File file, String relativePath) {
		DocFile docFile = new DocFile();
		docFile.setName(file.getName());
		docFile.setUrl(relativePath);
		docFile.setType(FileType.getFromFileEnding(file));
		for (DocLinkConfig linkConfig : docLinkConfigs) {
			docFile.addLink(new Link(linkConfig.getName(), linkConfig.getBaseUrl() + "/" + relativePath));
		}
		return docFile;
	}

	private void defineFeatureDetailsFromFileContent(Feature feature, File file) {
		// TODO: work in progress ...
		// Parse the file content line by line:
		// if a line starts with "# Milestone:" or "Milestone:" --> read the rest as milestone
		// if a line starts with "Order:" --> read the rest as orderIndex
		// if possible: try to extract a useful short description as well (first paragraph)
	}

	private String truncEnding(String fileName) {
		return fileName.split("\\.")[0];
	}

	/**
	 * Make a feature name readable, by applying following rules:
	 * * replace `-` by space
	 * * capitalize start of words
	 * @param name the name to make readable
	 */
	private String makeReadable(String name) {
		String nameWithSpaces = name.replaceAll("-", " ");
		return WordUtils.capitalize(nameWithSpaces);
	}

	/**
	 * Wait for all asynch writing to be finished.
	 */
	public void flush() {
		docuWriter.flush();
	}

}
