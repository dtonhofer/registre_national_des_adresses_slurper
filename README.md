registre_national_des_adresses_slurper
======================================

Code to read the "Registre National des Adresses" of Luxembourg into a memory structure

"Administration du Cadastre Luxembourg" makes availabe the database of addresses of Luxembourg on request.

See: http://www.act.public.lu/fr/parcelles-residences/registre-adresses/index.html

The data is a relational database dump, which one file per table.

The relationship are bit complex; see the documentation at:

http://www.act.public.lu/fr/publications/documents-techniques/specs_fichiers_adresses.pdf 

This program reads the files and simplifies the structure somewhat, creating a navigable in-memory structure.

As the contract under which the data is made available stipulates that you cannot distribute the data itself, 
this project does not contain the data files themselves. You have to drop them into the ''eu.qleap.address.db.slurp.resources'' package. 

How to use this
---------------

To run this, you need a recent Java Virtual Machine and the Groovy jar. You also need the slf4j-api.jar for logging.

Alternatively, you can load the project directly into the Eclipse IDE, as the repository actually comes with a .project file.

Then run the ''eu.qleap.address.db.slurp.entities.RunAll'' class. It contracts an in-memory structure called ''NationalTree'' 

So far, there is no code to dump this data structure into a database or XML.

