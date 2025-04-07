GitHub repository miner to find repositories with architectural information.
---

# Purpose of the program:
It mines repositories via GitHub. It tries to identify repositories with architectural information. To accomplish this the program analyzes random repositories by assigning points to them based on characteristics of the repository. After analyzing multiple repositories, one can see which repositories are most likely to contain architecture documentation based on the biggest number of points.
The points are given based on different rules. One rule is for example the existence of a "doc" folder.

---

# What do you need to execute the program?:
• Maven

• Java (I used OpenJDK 21.0.2)

• Best with a GitHub API key

• SambaNova API key, [see documentation](https://docs.sambanova.ai/sambastudio/latest/resources.html)

• Internet connection

---

# What are the API keys for?:
The programm uses lots of data, provided by the GitHub API. The number of requests is limited without authentication. I recommend trying it out with an API key, otherwise only about two repositories can be analyzed, until the rate limits reset after an hour. Here is the official documentation on how to get the key: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-fine-grained-personal-access-token.
The SambaNova API key is needed for a rule.
Both rules are expected in the environmental variables under GitHub_API and SambaNova_API.

---

# Set up:
• Clone commit ebfc562: https://github.com/Gollum225/monarch/tree/ebfc56261c9cc633fc2e3309b1f2c024b5eb2d98

• Run „mvn clean install“

• Set the access token. Usually the program takes it from the environment variables, but to simplify it, you can also just write it in the class as a String: src/main/java/repository_information/GitHub/GithubCommunication in line 50 for the GitHub API key and in src/main/java/controller/rules/LLMReadme.java in line 35 for the SambaNova API key.

• Configure runtime configuration. First argument args [0] (required): number of repositories to check, second argument args[1] (optional): search term, third argument args[2] (optional): quality points.

• Start programm in src/main/java/Main.java. The called checkRepos() method determines the number of repositories to analyze. (alternative start with Maven: "mvn compile", "mvn exec:java -Dexec.mainClass=Main")

---

# What should happen:
• A window called „Status“ opens. It shows the progress of the analyzed repositories.

• The console prints minor steps.

• After Termination a CSV file with the results is created at src/main/output/result.csv

---

#Configure
• The set of applied rules can be changed or motified in src/main/java/controller/RuleCollection.  Either create a new set and place it in line 28 or modify the setStandardRuleSet() method.
• A set of hig-quality repositories for debugging is in src/main/java/controller/RepoListManager.getTestRepos() and can be changed in line 148-149.
