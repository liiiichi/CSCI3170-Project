### Group Information
**Group Number**: 20

| Name  | SID  |
| :------------: | :------------: |
| Wong Shing Lok  | 1155156680  |
| Chan Siu Chung  |  1155157657 |
| Li Chi  |  1155172017 |


#### Director Tree
```bash
. 
├── BookOrderingSystem.java # source code
├── data  # for data insertion in `System interface > Insert Data`
│   ├── book_author.txt
│   ├── book.txt
│   ├── customer.txt
│   ├── ordering.txt
│   └── orders.txt
├── ojdbc7.jar # driver
├── README.md
└── system_date.txt # For storing system date
```

#### Compile
`cd` to your `BookOrderingSystem.java` diretory/

```bash
javac BookOrderingSystem.java
```
#### Run
```bash
java -classpath ojdbc7.jar:./ BookOrderingSystem
```
Make sure the `ojdbc7.jar` is in the same directory as the java program


##### Data Insertion
The data insertion can be just a relative path e.g. `data` 

