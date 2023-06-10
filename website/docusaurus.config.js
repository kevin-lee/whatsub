const algoliaConfig = require('./algolia.config.json');
const googleAnalyticsConfig = require('./google-analytics.config.json');

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

const isEmptyObject = obj => {
  for (field in obj) return false;
  return true;
};

const isSearchable = !isEmptyObject(algoliaConfig);
const hasGoogleAnalytics = !isEmptyObject(googleAnalyticsConfig);

/** @type {import('@docusaurus/types').DocusaurusConfig} */
const websiteConfig = {
  title: 'Whatsub',
  tagline: 'A tool for subtitles - conversion and syncing',
  url: 'https://whatsub.kevinly.dev',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.png',
  organizationName: 'kevin-lee', // Usually your GitHub org/user name.
  projectName: 'whatsub', // Usually your repo name.
  themeConfig: {
    image: 'img/poster.png',
    prism: {
      theme: lightCodeTheme,
      darkTheme: darkCodeTheme,
      additionalLanguages: [
        'java',
        'scala',
      ],
    },
    navbar: {
      title: 'Whatsub',
      logo: {
        alt: 'Whatsub Logo',
        src: 'img/whatsub-logo-32x32.png',
      },
      items: [
        {
          type: 'doc',
          docId: 'intro',
          position: 'left',
          label: 'Docs',
        },
        {
          href: 'https://github.com/kevin-lee/whatsub',
          position: 'right',
          className: 'header-github-link',
          'aria-label': 'GitHub',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Docs',
              to: '/docs/intro',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/kevin-lee/whatsub',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Whatsub written by <a href="https://github.com/kevin-lee" target="_blank"><b>Kevin Lee</b></a>, The website built with Docusaurus.
      <br /><span style="font-size: 80%;">Some Icons made by 
      <a href="https://www.freepik.com" title="Freepik">Freepik</a> and 
      <a href="https://www.flaticon.com/authors/iconixar" title="iconixar">iconixar</a>  
      from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a></div>
      `,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          showLastUpdateTime: true,
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
  plugins: [
    require.resolve('docusaurus-lunr-search'),
  ],
};

if (isSearchable) {
  websiteConfig['themeConfig']['algolia'] = algoliaConfig;
}
if (hasGoogleAnalytics) {
  websiteConfig['themeConfig']['googleAnalytics'] = googleAnalyticsConfig;
}

module.exports = websiteConfig;
