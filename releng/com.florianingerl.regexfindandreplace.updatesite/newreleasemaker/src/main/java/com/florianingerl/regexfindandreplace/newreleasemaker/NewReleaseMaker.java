package com.florianingerl.regexfindandreplace.newreleasemaker;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.AbortedByHookException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

import javax.swing.JOptionPane;

public class NewReleaseMaker {

	public static void main(String[] args) throws IOException, NoFilepatternException, GitAPIException {
		if (args.length != 1) {
			System.err.println(
					"Usage: newreleasemaker local repository for remote https://github.com/florianingerl/com.florian.regexfindandreplace");
			return;
		}
		
		System.out.println("NewReleaseMaker executed!");
		
		NewReleaseMaker nrm = new NewReleaseMaker(new File(args[0]));
		nrm.makeANewRelease();
	}

	private File dir;
	private Git git;

	private File latestEclipseRepo;

	private String newVersion;

	public NewReleaseMaker(File dir) throws IOException {
		this.dir = dir;
		this.git = Git.open(dir);

		latestEclipseRepo = new File(dir, "releng\\com.florianingerl.regexfindandreplace.updatesite\\releases\\latest");
	}

	public void makeANewRelease() throws NoFilepatternException, GitAPIException, IOException {
		deleteAllFilesIn(latestEclipseRepo);

		findOutNewVersion();

		addNewReleaseToGit();

		commit();

		push();

		System.out.println("Finished!");
	}

	private void deleteAllFilesIn(File dir) throws NoFilepatternException, GitAPIException {
		if (dir.listFiles() == null)
			return;
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				deleteAllFilesIn(f);
			}
			String relativePathTof = getRelativePathTo(f);
			git.rm().addFilepattern(relativePathTof).call();
		}
	}

	private String getRelativePathTo(File f) {
		String s=  dir.toPath().relativize(f.toPath()).toString();
		
		s = s.replace('\\', '/');
		System.out.println(s);
		return s;
	}

	private void findOutNewVersion() {
		File plugin = new File(dir,
				"releng\\com.florianingerl.regexfindandreplace.updatesite\\target\\repository\\plugins").listFiles()[0];
		newVersion = plugin.getName().substring("com.florianingerl.regexfindandreplace.plugin_".length(),
				plugin.getName().length() - ".jar".length());
	}

	private void addNewReleaseToGit() throws IOException, NoFilepatternException, GitAPIException {
		File eclipseRepo = new File(dir,
				"\\releng\\com.florianingerl.regexfindandreplace.updatesite\\target\\repository");

		File eclipseRepoWithNewVersion = new File(dir,
				"\\releng\\com.florianingerl.regexfindandreplace.updatesite\\releases\\" + newVersion);

		FileUtils.copyDirectory(eclipseRepo, latestEclipseRepo);
		FileUtils.copyDirectory(eclipseRepo, eclipseRepoWithNewVersion);

		git.add().addFilepattern(getRelativePathTo(latestEclipseRepo)).call();
		git.add().addFilepattern(getRelativePathTo(eclipseRepoWithNewVersion)).call();
	}

	private void commit() throws NoHeadException, NoMessageException, UnmergedPathsException,
			ConcurrentRefUpdateException, WrongRepositoryStateException, AbortedByHookException, GitAPIException {
		CommitCommand commit = git.commit();
		commit.setMessage("Makes a new release for version " + newVersion + "!").call();
	}

	private void push() throws InvalidRemoteException, TransportException, GitAPIException {
		PushCommand push = git.push();
		push.setCredentialsProvider(new CredentialsProvider() {

			@Override
			public boolean isInteractive() {
				return true;
			}

			@Override
			public boolean supports(CredentialItem... items) {
				return true;
			}

			@Override
			public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
				for (CredentialItem item : items) {
					if (item instanceof CredentialItem.Username) {
						((CredentialItem.Username) item).setValue("florianingerl");
					} else if (item instanceof CredentialItem.Password) {
						String password = JOptionPane.showInputDialog(null, "What's your password for GitHub?");
						((CredentialItem.Password) item).setValue(password.toCharArray());
					} else {
						return false;
					}
				}
				return true;
			}
		});
		push.call();
	}

}
