## 👤 About the Instructor

[![Ayan Dutta - Instructor](https://img-c.udemycdn.com/user/200_H/5007784_d6b8.jpg)](https://www.udemy.com/user/ayandutta/)

Hi, I’m **Ayan Dutta**, a Software Architect, Instructor, and Content Creator.  
I create practical, hands-on courses on **Java, Spring Boot, Debugging, Git, Python**, and more.

---

## 🌐 Connect With Me

- 💬 Slack Group: [Join Here](https://join.slack.com/t/learningfromexp/shared_invite/zt-1fnksxgd0-_jOdmIq2voEeMtoindhWrA)
- 📢 After joining, go to the #integration-testing-with-testcontainers-java-spring-boot channel  
- 📧 Email: j2eeexpert2015@gmail.com  
- 🔗 YouTube: [LearningFromExperience](https://www.youtube.com/@learningfromexperience)  
- 📝 Medium Blog: [@mrayandutta](https://medium.com/@mrayandutta)  
- 💼 LinkedIn: [Ayan Dutta](https://www.linkedin.com/in/ayan-dutta-a41091b/)

---

## 📺 Subscribe on YouTube

[![YouTube](https://img.shields.io/badge/Watch%20on%20YouTube-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://www.youtube.com/@learningfromexperience)

---

## 📚 Explore My Udemy Courses


### 🧩 Java Debugging Courses with Eclipse, IntelliJ IDEA, and VS Code

<table>
  <tr>
    <td>
      <a href="https://www.udemy.com/course/eclipse-debugging-techniques-and-tricks">
        <img src="https://img-c.udemycdn.com/course/480x270/417118_3afa_4.jpg" width="250"><br/>
        <b>Eclipse Debugging Techniques</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/java-debugging-with-intellij-idea">
        <img src="https://img-c.udemycdn.com/course/480x270/2608314_47e4.jpg" width="250"><br/>
        <b>Java Debugging With IntelliJ</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/java-debugging-with-visual-studio-code-the-ultimate-guide">
        <img src="https://img-c.udemycdn.com/course/480x270/5029852_d692_3.jpg" width="250"><br/>
        <b>Java Debugging with VS Code</b>
      </a>
    </td>
  </tr>
</table>

---

### 💡 Java Productivity & Patterns

<table>
  <tr>
    <td>
      <a href="https://www.udemy.com/course/intellij-idea-tips-tricks-boost-your-java-productivity">
        <img src="https://img-c.udemycdn.com/course/480x270/6180669_7726.jpg" width="250"><br/>
        <b>IntelliJ IDEA Tips & Tricks</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/design-patterns-in-javacreational">
        <img src="https://img-c.udemycdn.com/course/480x270/779796_5770_2.jpg" width="250"><br/>
        <b>Creational Design Patterns</b>
      </a>
    </td>
  </tr>
</table>

---

### 🐍 Python Debugging Courses

<table>
  <tr>
    <td>
      <a href="https://www.udemy.com/course/learn-python-debugging-with-pycharm-ide">
        <img src="https://img-c.udemycdn.com/course/480x270/4840890_12a3_2.jpg" width="250"><br/>
        <b>Python Debugging With PyCharm</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/python-debugging-with-visual-studio-code">
        <img src="https://img-c.udemycdn.com/course/480x270/5029842_d36f.jpg" width="250"><br/>
        <b>Python Debugging with VS Code</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/get-started-with-python-debugging-in-visual-studio-code">
        <img src="https://img-c.udemycdn.com/course/480x270/6412275_a17d.jpg" width="250"><br/>
        <b>Python Debugging (Free)</b>
      </a>
    </td>
  </tr>
</table>

---

### 🛠 Git & GitHub Courses

<table>
  <tr>
    <td>
      <a href="https://www.udemy.com/course/getting-started-with-github-desktop">
        <img src="https://img-c.udemycdn.com/course/480x270/6112307_3b4e_2.jpg" width="250"><br/>
        <b>GitHub Desktop Guide</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/learn-to-use-git-and-github-with-eclipse-a-complete-guide">
        <img src="https://img-c.udemycdn.com/course/480x270/3369428_995b.jpg" width="250"><br/>
        <b>Git & GitHub with Eclipse</b>
      </a>
    </td>
  </tr>
</table>


---

## 🐳 Preparing a Custom PostgreSQL Image on GHCR

These steps show how to prepare and test a custom PostgreSQL image in GitHub Container Registry (GHCR) to be used in Testcontainers.

### 🔐 Step 1: Generate a GitHub Personal Access Token (GHCR_PAT)

1. Go to: https://github.com/settings/tokens?type=beta  
2. Click: Generate new token → Classic  
3. Select scopes:
   - ✅ read:packages  
   - ✅ write:packages  
4. Copy the generated token and save it securely

You’ll use this token as the password when logging in to GHCR.

---

### 🔧 Step 2: Tag and Push to GHCR

Using your own GitHub username:

```bash
# Pull the official Postgres image
docker pull postgres:15

# Tag the image
docker tag postgres:15 ghcr.io/<your-github-username>/approved-images/postgres:15
# Example:
docker tag postgres:15 ghcr.io/j2eeexpert2015/approved-images/postgres:15

# Login to GHCR
echo <GHCR_PAT> | docker login ghcr.io -u <your-github-username> --password-stdin
# Example:
echo <GHCR_PAT> | docker login ghcr.io -u j2eeexpert2015 --password-stdin

# Push to GHCR
docker push ghcr.io/<your-github-username>/approved-images/postgres:15
# Example:
docker push ghcr.io/j2eeexpert2015/approved-images/postgres:15
```

---

### 🚫 Step 3: Test Access Without Authentication

To simulate an unauthenticated environment:

```bash
# Logout from GHCR
docker logout ghcr.io

# Remove local image
docker rmi ghcr.io/<your-github-username>/approved-images/postgres:15
# Example:
docker rmi ghcr.io/j2eeexpert2015/approved-images/postgres:15

# Attempt pull without auth
docker pull ghcr.io/<your-github-username>/approved-images/postgres:15
# Example:
docker pull ghcr.io/j2eeexpert2015/approved-images/postgres:15
```

✅ This pull should fail if the image is private, confirming access is restricted without credentials.


