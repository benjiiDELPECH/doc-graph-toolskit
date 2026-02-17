# Sample Data

This directory contains sample PDF documents for testing the DocGraph POC system.

## Creating Test PDFs

You can create test PDFs in several ways:

1. **From text file:**
   ```bash
   # On Linux with LibreOffice
   libreoffice --headless --convert-to pdf sample.txt
   ```

2. **Online converters:**
   - Use any online text-to-PDF converter
   - Upload the sample.txt file

3. **Print to PDF:**
   - Open sample.txt in any text editor
   - Use "Print to PDF" option

## Sample Content

The sample.txt file contains various entity types:
- Persons: John Smith, Jane Doe
- Organizations: Acme Corp, Tech Solutions LLC
- Locations: New York, NY, San Francisco, CA
- Dates and events

Use these PDFs to test the entity extraction and graph visualization features.
