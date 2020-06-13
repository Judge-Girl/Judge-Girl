# Judge-K8S

## DEMO
![DEMO](https://i.imgur.com/MZeadP5.png)

## Background

Currently, students depend on Judge girl system to submit the codes for homework or practice purposes. Judge girl works in its unscalable manner due to its heavy coupling to the OS and file system. However, this issue was intensified by those Judge girl’s problems which require GPUs. Judge girl schedules code-submission  in a FIFO fashion, to one hard-coded computing node for execution. 

## Objective

In this project, we are not going to renovate the whole legacy Judge girl system. We provide a new Judge Girl component named Judge-K8S, which is only focusing on GPU-related  submission scheduling and literally with the help of Kubernetes. 

Judge-K8S must not be coupling with the legacy Judge girl system, because the legacy code is not well-tested and unstable. Judge-K8S only interacts with the Judge girl system via HTTP api to perform some necessary functions, e.g.  login.

Judge-K8S is primarily for research purposes and class assignment purposes. Judge-K8S should be light and minimal, covering only the minimal requirements, and is made with a simple demo web page.

The following picture illustrates the overview of the project.


![Overview](https://i.imgur.com/3HVJsUi.png)
