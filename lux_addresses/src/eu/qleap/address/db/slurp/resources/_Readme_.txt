This package contains the files obtained (for free) from "Administration du Cadastre".

This is a relational database dump. 

The relationship are bit complex though; see the documentation.

Our program reads the files and simplifies the structure somewhat, creating a navigable
in-memory structure.

To obtain the raw data, see this page

http://www.act.public.lu/fr/parcelles-residences/registre-adresses/index.html

Rhe contract stipulates that you cannot distribute the data itself, this directory
may be empty.

Otherwise it contains the files:

DISTRICT         - Districts of Luxembourg
CANTON           - Cantons of Luxembourg
COMMUNE          - Communes of Luxembourg 
LOCALITE         - Towns and Villages
QUARTIER         - Suburbs of the City of Luxembourg 
ALIAS.LOCALITE   - Aliases for towns and villages
RUE              - Streets of Luxembourg
ALIAS.RUE        - Aliases for streets
CODEPT           - Postal codes
CPTCH            - Codes used by Ponts & Chaussées (i.e. Road identifiers)
IMMEUBLE         - Addresses of buildings
IMMDESIG         - Addresses of buildings, where a description for the building exists
TR.DICACOLO.RUCP - Combination, forming the administrative hierarchy
index.txt
oracle.ddl

And also:

"Spécification des fichiers.pdf", which is the specification. It can be found at

http://www.act.public.lu/fr/publications/documents-techniques/specs_fichiers_adresses.pdf 




