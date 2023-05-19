# clone-all
Having trouble cloning your REPOs? This little application is here to help. 

How to set it up? 

1. Install IntelliJ Idea with Java 17.
2. Install git.
3. Generate your ssh key and add it to your Gitlab or Bitbucket profile
4. Generate an access token 
5. Add the details required in the test/resources/*.properties
6. Run the unit test for either Bitbucket or Gitlab.

Debugging

1. If your repos fail to generate and git exits with a code 128 you will need to run a manual git clone to debug it. It is usually access related issues